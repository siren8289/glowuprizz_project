package com.glowup.crm.api;

import com.glowup.crm.auth.Admin;
import com.glowup.crm.auth.AdminRepository;
import com.glowup.crm.campaign.Campaign;
import com.glowup.crm.campaign.CampaignRepository;
import com.glowup.crm.campaign.CustomForm;
import com.glowup.crm.campaign.CustomFormRepository;
import com.glowup.crm.distribution.Channel;
import com.glowup.crm.distribution.DistributionLink;
import com.glowup.crm.distribution.DistributionLinkRepository;
import com.glowup.crm.form.FormTemplate;
import com.glowup.crm.form.FormTemplateRepository;
import com.glowup.crm.lead.Lead;
import com.glowup.crm.lead.LeadRepository;
import com.glowup.crm.tracking.VisitRepository;
import com.glowup.crm.security.TokenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.security.SecureRandom;
import java.util.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class AdminController {
    private final AdminRepository admins;
    private final FormTemplateRepository templates;
    private final CampaignRepository campaigns;
    private final CustomFormRepository customForms;
    private final DistributionLinkRepository links;
    private final LeadRepository leads;
    private final VisitRepository visits;
    private final TokenService tokens;
    private final SecureRandom random = new SecureRandom();

    public AdminController(AdminRepository admins, FormTemplateRepository templates, CampaignRepository campaigns, CustomFormRepository customForms,
                           DistributionLinkRepository links, LeadRepository leads, VisitRepository visits,
                           TokenService tokens) {
        this.admins = admins; this.templates = templates; this.campaigns = campaigns; this.customForms = customForms; this.links = links;
        this.leads = leads; this.visits = visits; this.tokens = tokens;
    }

    @PostMapping("/auth/login")
    public Map<String, String> login(@Valid @RequestBody LoginRequest request) {
        Admin admin = admins.findByUsername(request.username()).orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!new BCryptPasswordEncoder().matches(request.password(), admin.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return Map.of("token", tokens.issue(admin.getId()), "username", admin.getUsername());
    }

    @PostMapping(value = "/forms", consumes = "multipart/form-data")
    public FormResponse uploadForm(@RequestPart("file") MultipartFile file) throws java.io.IOException {
        return saveTemplate(Objects.requireNonNull(file.getOriginalFilename()), new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8));
    }

    @PostMapping(value = "/forms", consumes = "application/json")
    public FormResponse createForm(@Valid @RequestBody FormRequest request) {
        return saveTemplate(request.name(), request.htmlContent());
    }

    @GetMapping("/forms")
    public List<FormResponse> listForms() {
        return templates.findAll().stream().map(FormResponse::from).toList();
    }

    @GetMapping("/forms/{formId}")
    public FormResponse getForm(@PathVariable Long formId) {
        return FormResponse.from(template(formId));
    }

    @PostMapping("/campaigns")
    public CampaignResponse createCampaign(@Valid @RequestBody CampaignRequest request) {
        Campaign campaign = campaigns.save(new Campaign(request.name().trim()));
        campaign.setCustomForm(customForms.save(new CustomForm(campaign, template(request.templateId()))));
        return CampaignResponse.from(campaign, links.findByCampaignId(campaign.getId()));
    }

    @GetMapping("/campaigns")
    public List<CampaignResponse> listCampaigns() {
        return campaigns.findAll().stream().map(c -> CampaignResponse.from(c, links.findByCampaignId(c.getId()))).toList();
    }

    @GetMapping("/campaigns/{campaignId}")
    public CampaignResponse getCampaign(@PathVariable Long campaignId) {
        Campaign campaign = campaign(campaignId);
        return CampaignResponse.from(campaign, links.findByCampaignId(campaignId));
    }

    @PostMapping({"/campaigns/{campaignId}/distribution-links", "/campaigns/{campaignId}/distributions"})
    public LinkResponse createDistributionLink(@PathVariable Long campaignId, @Valid @RequestBody LinkRequest request) {
        DistributionLink link = links.save(new DistributionLink(campaign(campaignId), request.channel(), randomToken()));
        return LinkResponse.from(link);
    }

    @GetMapping("/campaigns/{campaignId}/leads")
    public List<LeadResponse> listLeads(@PathVariable Long campaignId) {
        campaign(campaignId);
        return leads.findByCampaignIdOrderBySubmittedAtDesc(campaignId).stream().map(LeadResponse::from).toList();
    }

    @GetMapping({"/campaigns/{campaignId}/statistics", "/campaigns/{campaignId}/analytics"})
    public StatisticsResponse statistics(@PathVariable Long campaignId) {
        campaign(campaignId);
        return statisticsFor(campaignId, null);
    }

    @GetMapping({"/campaigns/{campaignId}/statistics/channels", "/campaigns/{campaignId}/analytics/channels"})
    public List<ChannelStatisticsResponse> channelStatistics(@PathVariable Long campaignId) {
        campaign(campaignId);
        return links.findByCampaignId(campaignId).stream()
                .map(link -> new ChannelStatisticsResponse(link.getChannel(), link.getToken(), statisticsFor(campaignId, link)))
                .toList();
    }

    private FormResponse saveTemplate(String name, String html) {
        if (!name.toLowerCase(Locale.ROOT).endsWith(".html")) throw new IllegalArgumentException("Only .html files are accepted");
        if (html == null || html.isBlank() || html.length() > 1_000_000 || !html.toLowerCase(Locale.ROOT).contains("<form")) {
            throw new IllegalArgumentException("HTML must contain a form and be at most 1 MB");
        }
        return FormResponse.from(templates.save(new FormTemplate(name, html)));
    }

    private FormTemplate template(Long id) {
        return templates.findById(id).orElseThrow(() -> new NoSuchElementException("Form template not found"));
    }
    private Campaign campaign(Long id) {
        return campaigns.findById(id).orElseThrow(() -> new NoSuchElementException("Campaign not found"));
    }
    private String randomToken() {
        byte[] bytes = new byte[18]; random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    private StatisticsResponse statisticsFor(Long campaignId, DistributionLink link) {
        long visitCount = link == null ? visits.countByCampaignId(campaignId) : visits.countByDistributionLinkId(link.getId());
        long visitors = link == null ? visits.countUniqueVisitorsByCampaignId(campaignId) : visits.countUniqueVisitorsByDistributionLinkId(link.getId());
        long applications = link == null ? leads.countByCampaignId(campaignId) : leads.countByDistributionLinkId(link.getId());
        return new StatisticsResponse(visitCount, visitors, applications, visitors == 0 ? 0 : Math.round(applications * 10000.0 / visitors) / 100.0);
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record FormRequest(@NotBlank String name, @NotBlank String htmlContent) {}
    public record CampaignRequest(@NotBlank String name, @NotNull Long templateId) {}
    public record LinkRequest(@NotNull Channel channel) {}
    public record FormResponse(Long id, String name, java.time.Instant createdAt) {
        static FormResponse from(FormTemplate form) { return new FormResponse(form.getId(), form.getName(), form.getCreatedAt()); }
    }
    public record CampaignResponse(Long id, String name, Long customFormId, Long templateId, String templateName, java.time.Instant createdAt, List<LinkResponse> distributionLinks) {
        static CampaignResponse from(Campaign campaign, List<DistributionLink> links) {
            return new CampaignResponse(campaign.getId(), campaign.getName(), campaign.getCustomForm().getId(), campaign.getCustomForm().getTemplate().getId(), campaign.getCustomForm().getTemplate().getName(),
                    campaign.getCreatedAt(), links.stream().map(LinkResponse::from).toList());
        }
    }
    public record LinkResponse(Long id, Channel channel, String token, String url) {
        static LinkResponse from(DistributionLink link) { return new LinkResponse(link.getId(), link.getChannel(), link.getToken(), "/r/" + link.getToken()); }
    }
    public record StatisticsResponse(long visits, long uniqueVisitors, long applications, double conversionRate) {}
    public record ChannelStatisticsResponse(Channel channel, String token, StatisticsResponse statistics) {}
    public record LeadResponse(Long id, String channel, String data, java.time.Instant submittedAt) {
        static LeadResponse from(Lead lead) { return new LeadResponse(lead.getId(), lead.getDistributionLink().getChannel().name(), lead.getData(), lead.getSubmittedAt()); }
    }
}
