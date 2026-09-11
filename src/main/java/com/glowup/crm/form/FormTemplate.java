package com.glowup.crm.form;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "form_templates")
public class FormTemplate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String htmlContent;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected FormTemplate() {}
    public FormTemplate(String name, String htmlContent) { this.name = name; this.htmlContent = htmlContent; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getHtmlContent() { return htmlContent; }
    public Instant getCreatedAt() { return createdAt; }
}
