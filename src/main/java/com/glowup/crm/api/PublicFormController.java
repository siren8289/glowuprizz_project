package com.glowup.crm.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.glowup.crm.distribution.DistributionLink;
import com.glowup.crm.distribution.DistributionLinkRepository;
import com.glowup.crm.lead.Lead;
import com.glowup.crm.lead.LeadRepository;
import com.glowup.crm.tracking.Visit;
import com.glowup.crm.tracking.VisitRepository;
import com.glowup.crm.tracking.Visitor;
import com.glowup.crm.tracking.VisitorRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.MultiValueMap;

@RestController
public class PublicFormController {
    private static final Pattern FORM_TAG = Pattern.compile("(?is)<form\\b([^>]*)>");
    private final DistributionLinkRepository links;
    private final VisitorRepository visitors;
    private final VisitRepository visits;
    private final LeadRepository leads;
    private final ObjectMapper objectMapper;

    public PublicFormController(DistributionLinkRepository links, VisitorRepository visitors, VisitRepository visits,
                                LeadRepository leads, ObjectMapper objectMapper) {
        this.links = links; this.visitors = visitors; this.visits = visits; this.leads = leads; this.objectMapper = objectMapper;
    }

    @GetMapping("/r/{token}")
    public ResponseEntity<Void> trackAndRedirect(@PathVariable String token, HttpServletRequest request) {
        DistributionLink link = link(token);
        String visitorId = visitorId(request);
        visitors.findById(visitorId).orElseGet(() -> visitors.save(new Visitor(visitorId)));
        visits.save(new Visit(link.getCampaign(), link, visitors.getReferenceById(visitorId)));
        ResponseCookie cookie = ResponseCookie.from("lm_visitor", visitorId).httpOnly(true).sameSite("Lax").path("/").maxAge(31536000).build();
        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.SET_COOKIE, cookie.toString())
                .location(URI.create("/public/forms/" + link.getCampaign().getCustomForm().getTemplate().getId() + "?d=" + token + "&v=" + visitorId)).build();
    }

    @GetMapping(value = "/public/forms/{formId}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> publicForm(@PathVariable Long formId, @RequestParam("d") String token, @RequestParam("v") String visitorId) {
        DistributionLink link = link(token);
        if (!link.getCampaign().getCustomForm().getTemplate().getId().equals(formId) || !validVisitorId(visitorId)) {
            throw new IllegalArgumentException("Invalid public form request");
        }
        String action = "/public/forms/" + formId + "/submissions?d=" + urlEncode(token) + "&v=" + urlEncode(visitorId);
        String source = replaceFormAction(link.getCampaign().getCustomForm().getTemplate().getHtmlContent(), action);
        String document = """
                <!doctype html><html><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
                <title>신청하기</title><style>html,body,iframe{margin:0;width:100%%;height:100%%;border:0}</style></head>
                <body><iframe sandbox="allow-forms" referrerpolicy="no-referrer" srcdoc="%s"></iframe></body></html>
                """.formatted(htmlAttributeEscape(source));
        return ResponseEntity.ok().header("Content-Security-Policy",
                "default-src 'none'; style-src 'unsafe-inline'; frame-src 'self'; base-uri 'none'; frame-ancestors *")
                .body(document);
    }

    @PostMapping(value = "/public/forms/{formId}/submissions", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> submit(@PathVariable Long formId, @RequestParam("d") String token, @RequestParam("v") String visitorId,
                                         @RequestParam MultiValueMap<String, String> fields) {
        DistributionLink link = link(token);
        if (!link.getCampaign().getCustomForm().getTemplate().getId().equals(formId) || !validVisitorId(visitorId)) {
            throw new IllegalArgumentException("Invalid submission request");
        }
        Map<String, List<String>> payload = new LinkedHashMap<>();
        fields.forEach((key, values) -> {
            if (!key.startsWith("_") && !key.equals("website")) payload.put(key, values);
        });
        if (fields.containsKey("website") || payload.isEmpty() || payload.values().stream().flatMap(Collection::stream).allMatch(value -> value == null || value.isBlank())) {
            throw new IllegalArgumentException("Please complete the required application fields");
        }
        if (leads.existsByDistributionLinkIdAndVisitorId(link.getId(), visitorId)) {
            throw new IllegalArgumentException("This application has already been submitted");
        }
        Visitor visitor = visitors.findById(visitorId).orElseThrow(() -> new IllegalArgumentException("Visitor session not found"));
        try {
            leads.save(new Lead(link.getCampaign(), link, visitor, objectMapper.writeValueAsString(payload)));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot save application data", exception);
        }
        return ResponseEntity.ok("<!doctype html><html lang=\"ko\"><meta charset=\"utf-8\"><title>신청 완료</title><body><h1>신청이 완료되었습니다.</h1><p>소중한 정보를 확인 후 안내드리겠습니다.</p></body></html>");
    }

    private DistributionLink link(String token) {
        return links.findByToken(token).orElseThrow(() -> new NoSuchElementException("Distribution link not found"));
    }
    private String visitorId(HttpServletRequest request) {
        if (request.getCookies() != null) for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("lm_visitor") && validVisitorId(cookie.getValue())) return cookie.getValue();
        }
        return UUID.randomUUID().toString().replace("-", "");
    }
    private boolean validVisitorId(String value) { return value != null && value.matches("[a-f0-9]{32}"); }
    private String urlEncode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }
    private String replaceFormAction(String html, String action) {
        Matcher matcher = FORM_TAG.matcher(html);
        if (!matcher.find()) throw new IllegalArgumentException("The template does not contain a form");
        String attributes = matcher.group(1).replaceAll("(?i)\\s+(action|method)\\s*=\\s*(\"[^\"]*\"|'[^']*'|[^\\s>]+)", "");
        String replacement = "<form" + attributes + " method=\"post\" action=\"" + action + "\">";
        return matcher.replaceFirst(Matcher.quoteReplacement(replacement));
    }
    private String htmlAttributeEscape(String value) {
        return value.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
