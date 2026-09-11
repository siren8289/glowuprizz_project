package com.glowup.crm.tracking;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "visitors")
public class Visitor {
    @Id @Column(length = 64)
    private String id;
    @Column(nullable = false, updatable = false)
    private Instant firstSeenAt = Instant.now();
    protected Visitor() {}
    public Visitor(String id) { this.id = id; }
    public String getId() { return id; }
}
