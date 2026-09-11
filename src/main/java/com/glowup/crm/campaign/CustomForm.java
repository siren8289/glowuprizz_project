package com.glowup.crm.campaign;

import com.glowup.crm.form.FormTemplate;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "custom_forms")
public class CustomForm {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(optional = false)
    @JoinColumn(name = "campaign_id", nullable = false, unique = true)
    private Campaign campaign;
    @ManyToOne(optional = false)
    private FormTemplate template;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected CustomForm() {}
    public CustomForm(Campaign campaign, FormTemplate template) {
        this.campaign = campaign;
        this.template = template;
    }
    public Long getId() { return id; }
    public Campaign getCampaign() { return campaign; }
    public FormTemplate getTemplate() { return template; }
    public Instant getCreatedAt() { return createdAt; }
}
