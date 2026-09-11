package com.glowup.crm.lead;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadRepository extends JpaRepository<Lead, Long> {
    List<Lead> findByCampaignIdOrderBySubmittedAtDesc(Long campaignId);
    long countByCampaignId(Long campaignId);
    long countByDistributionLinkId(Long distributionLinkId);
    boolean existsByDistributionLinkIdAndVisitorId(Long distributionLinkId, String visitorId);
}
