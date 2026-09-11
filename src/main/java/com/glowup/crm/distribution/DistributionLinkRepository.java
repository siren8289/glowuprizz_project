package com.glowup.crm.distribution;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DistributionLinkRepository extends JpaRepository<DistributionLink, Long> {
    Optional<DistributionLink> findByToken(String token);
    List<DistributionLink> findByCampaignId(Long campaignId);
}
