package com.lazari.filler_med.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class AboutSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String subtitle;
    @Column(length = 4000)
    private String content;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_settings_id")
    private SiteSettings siteSettings;
}
