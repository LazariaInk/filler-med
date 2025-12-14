package com.lazari.filler_med.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class SiteSettings {
    @Id
    private Long id = 1L;
    private String facebookUrl;
    private String phone1;
    private String phone2;
    private String address;
    private String regCom;
    private String cif;
    @OneToMany(mappedBy = "siteSettings", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "position_index")
    private List<AboutSection> aboutSections = new ArrayList<>();
}

