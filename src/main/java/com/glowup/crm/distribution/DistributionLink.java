package com.glowup.crm.distribution;

import com.glowup.crm.campaign.Campaign;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "distribution_links")
public class DistributionLink {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Campaign campaign;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Channel channel;
    @Column(nullable = false, unique = true, length = 64)
    private String token;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected DistributionLink() {}
    public DistributionLink(Campaign campaign, Channel channel, String token) {
        this.campaign = campaign; this.channel = channel; this.token = token;
    }
    public Long getId() { return id; }
    public Campaign getCampaign() { return campaign; }
    public Channel getChannel() { return channel; }
    public String getToken() { return token; }
    public Instant getCreatedAt() { return createdAt; }
}
