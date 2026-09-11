package com.glowup.crm.lead;

import com.glowup.crm.campaign.Campaign;
import com.glowup.crm.distribution.DistributionLink;
import com.glowup.crm.tracking.Visitor;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "leads")
public class Lead {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private Campaign campaign;
    @ManyToOne(optional = false) private DistributionLink distributionLink;
    @ManyToOne(optional = false) private Visitor visitor;
    @Column(nullable = false, columnDefinition = "TEXT") private String data;
    @Column(nullable = false, updatable = false) private Instant submittedAt = Instant.now();
    protected Lead() {}
    public Lead(Campaign campaign, DistributionLink link, Visitor visitor, String data) {
        this.campaign = campaign; this.distributionLink = link; this.visitor = visitor; this.data = data;
    }
    public Long getId() { return id; }
    public Campaign getCampaign() { return campaign; }
    public DistributionLink getDistributionLink() { return distributionLink; }
    public String getData() { return data; }
    public Instant getSubmittedAt() { return submittedAt; }
}
