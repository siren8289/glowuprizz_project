package com.glowup.crm.config;

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
import com.glowup.crm.tracking.Visit;
import com.glowup.crm.tracking.VisitRepository;
import com.glowup.crm.tracking.Visitor;
import com.glowup.crm.tracking.VisitorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SeedData {
    @Bean
    CommandLineRunner runSeedData(AdminRepository admins, FormTemplateRepository templates, CampaignRepository campaigns,
                               CustomFormRepository customForms, DistributionLinkRepository links,
                               VisitorRepository visitors, VisitRepository visits, LeadRepository leads,
                               @Value("${app.seed-demo-data}") boolean seedDemoData) {
        return ignored -> {
            if (admins.count() == 0) {
                admins.save(new Admin("admin", new BCryptPasswordEncoder().encode("admin123")));
            }
            if (!seedDemoData) return;
            if (campaigns.count() < 1) {
                seedCampaign("wellness-lead.html", "9월 웰니스 가이드", "무료 웰니스 가이드 신청",
                        "demoInstagramLead2026", Channel.INSTAGRAM, "a1b2c3d4e5f6478890a1b2c3d4e5f6a7",
                        templates, campaigns, customForms, links, visitors, visits, leads);
            }
            if (campaigns.count() < 2) {
                seedCampaign("beauty-lead.html", "가을 뷰티 루틴", "퍼스널 뷰티 루틴 신청",
                        "demoThreadsLead2026", Channel.THREADS, "b2c3d4e5f6a7488990b2c3d4e5f6a7b8",
                        templates, campaigns, customForms, links, visitors, visits, leads);
            }
            if (campaigns.count() < 3) {
                seedCampaign("career-lead.html", "커리어 성장 뉴스레터", "커리어 인사이트 뉴스레터 신청",
                        "demoYouTubeLead2026", Channel.YOUTUBE, "c3d4e5f6a7b8489090c3d4e5f6a7b8c9",
                        templates, campaigns, customForms, links, visitors, visits, leads);
            }
            if (templates.count() < 3) {
                templates.save(new FormTemplate("event-lead.html", """
                        <!doctype html><html lang="ko"><body><form>
                        <h1>무료 온라인 세미나 신청</h1><p>실무 전략을 라이브로 공유합니다.</p>
                        <label>이름 <input name="name" required></label>
                        <label>이메일 <input name="email" type="email" required></label>
                        <button type="submit">좌석 예약하기</button></form></body></html>
                        """));
            }
        };
    }

    private void seedCampaign(String fileName, String campaignName, String title, String token, Channel channel, String visitorId,
                              FormTemplateRepository templates, CampaignRepository campaigns, CustomFormRepository customForms,
                              DistributionLinkRepository links, VisitorRepository visitors, VisitRepository visits, LeadRepository leads) {
        FormTemplate template = templates.save(new FormTemplate(fileName, """
                <!doctype html><html lang="ko"><body><form>
                <h1>%s</h1><label>이름 <input name="name" required></label>
                <label>이메일 <input name="email" type="email" required></label>
                <button type="submit">신청하기</button></form></body></html>
                """.formatted(title)));
        Campaign campaign = campaigns.save(new Campaign(campaignName));
        campaign.setCustomForm(customForms.save(new CustomForm(campaign, template)));
        DistributionLink link = links.save(new DistributionLink(campaign, channel, token));
        Visitor visitor = visitors.save(new Visitor(visitorId));
        visits.save(new Visit(campaign, link, visitor));
        visits.save(new Visit(campaign, link, visitor));
        leads.save(new Lead(campaign, link, visitor, "{\"name\":[\"데모 신청자\"],\"email\":[\"demo@example.com\"]}"));
    }
}
