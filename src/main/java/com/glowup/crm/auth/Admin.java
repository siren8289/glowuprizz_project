package com.glowup.crm.auth;

import jakarta.persistence.*;

@Entity
@Table(name = "admins")
public class Admin {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false)
    private String passwordHash;

    protected Admin() {}
    public Admin(String username, String passwordHash) { this.username = username; this.passwordHash = passwordHash; }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
}
