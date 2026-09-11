package com.glowup.crm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LeadMagnetApplicationTests {
    @LocalServerPort private int port;
    @Autowired private TestRestTemplate client;

    @Test
    void adminCanCreateAndMeasureLeadMagnetFlow() {
        String token = login();
        HttpHeaders adminHeaders = new HttpHeaders();
        adminHeaders.setBearerAuth(token);
        adminHeaders.setContentType(MediaType.APPLICATION_JSON);

        Map<?, ?> form = client.postForObject(url("/api/forms"),
                new HttpEntity<>(Map.of("name", "lead.html", "htmlContent", "<form><input name='email' required></form>"), adminHeaders), Map.class);
        assertThat(form.get("id")).isNotNull();

        Map<?, ?> campaign = client.postForObject(url("/api/campaigns"),
                new HttpEntity<>(Map.of("name", "September Lead", "templateId", form.get("id")), adminHeaders), Map.class);
        Number campaignId = (Number) campaign.get("id");
        Map<?, ?> link = client.postForObject(url("/api/campaigns/" + campaignId + "/distribution-links"),
                new HttpEntity<>(Map.of("channel", "INSTAGRAM"), adminHeaders), Map.class);

        HttpHeaders publicHeaders = new HttpHeaders();
        publicHeaders.setAccept(java.util.List.of(MediaType.ALL));
        ResponseEntity<String> redirect = client.exchange(url((String) link.get("url")), HttpMethod.GET,
                new HttpEntity<>(publicHeaders), String.class);
        assertThat(redirect.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(redirect.getBody()).contains("sandbox");
        String visitorId = java.util.regex.Pattern.compile("v=([a-f0-9]{32})").matcher(redirect.getBody())
                .results().findFirst().orElseThrow().group(1);

        MultiValueMap<String, String> values = new LinkedMultiValueMap<>();
        values.add("email", "visitor@example.com");
        HttpHeaders formHeaders = new HttpHeaders();
        formHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ResponseEntity<String> submission = client.postForEntity(url("/public/forms/" + form.get("id") + "/submissions?d=" + link.get("token") + "&v=" + visitorId),
                new HttpEntity<>(values, formHeaders), String.class);
        assertThat(submission.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map<?, ?> stats = client.exchange(url("/api/campaigns/" + campaignId + "/statistics"), HttpMethod.GET,
                new HttpEntity<>(adminHeaders), Map.class).getBody();
        assertThat(((Number) stats.get("visits")).longValue()).isEqualTo(1);
        assertThat(((Number) stats.get("uniqueVisitors")).longValue()).isEqualTo(1);
        assertThat(((Number) stats.get("applications")).longValue()).isEqualTo(1);
    }

    private String login() {
        Map<?, ?> response = client.postForObject(url("/api/auth/login"), Map.of("username", "admin", "password", "admin123"), Map.class);
        return (String) response.get("token");
    }
    private String url(String path) { return "http://localhost:" + port + path; }
}
