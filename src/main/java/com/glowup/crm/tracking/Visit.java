package com.glowup.crm.tracking;

import com.glowup.crm.campaign.Campaign;
import com.glowup.crm.distribution.DistributionLink;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "visits")
public class Visit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) private Campaign campaign;
    @ManyToOne(optional = false) private DistributionLink distributionLink;
    @ManyToOne(optional = false) private Visitor visitor;
    @Column(nullable = false, updatable = false) private Instant visitedAt = Instant.now();
    protected Visit() {}
    public Visit(Campaign campaign, DistributionLink link, Visitor visitor) {
        this.campaign = campaign; this.distributionLink = link; this.visitor = visitor;
    }
}
