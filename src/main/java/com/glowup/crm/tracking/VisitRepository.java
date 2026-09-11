package com.glowup.crm.tracking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VisitRepository extends JpaRepository<Visit, Long> {
    long countByCampaignId(Long campaignId);
    @Query("select count(distinct v.visitor.id) from Visit v where v.campaign.id = :campaignId")
    long countUniqueVisitorsByCampaignId(Long campaignId);
    long countByDistributionLinkId(Long distributionLinkId);
    @Query("select count(distinct v.visitor.id) from Visit v where v.distributionLink.id = :linkId")
    long countUniqueVisitorsByDistributionLinkId(Long linkId);
}
