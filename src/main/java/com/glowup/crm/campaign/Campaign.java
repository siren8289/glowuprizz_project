package com.glowup.crm.campaign;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "campaigns")
public class Campaign {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @OneToOne(mappedBy = "campaign", fetch = FetchType.EAGER)
    private CustomForm customForm;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Campaign() {}
    public Campaign(String name) { this.name = name; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public CustomForm getCustomForm() { return customForm; }
    public void setCustomForm(CustomForm customForm) { this.customForm = customForm; }
    public Instant getCreatedAt() { return createdAt; }
}
