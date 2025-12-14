package com.lazari.filler_med.service;

import com.lazari.filler_med.model.AboutSection;
import com.lazari.filler_med.model.SiteSettings;
import com.lazari.filler_med.repostiory.SiteSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SiteSettingsService {

    private final SiteSettingsRepository repository;

    public SiteSettingsService(SiteSettingsRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public SiteSettings getSettings() {
        return repository.findById(1L).orElseGet(() -> {
            SiteSettings s = new SiteSettings();
            return repository.save(s);
        });
    }

    @Transactional
    public void saveSettings(SiteSettings updated) {
        updated.setAboutSections(
                updated.getAboutSections() == null ? new java.util.ArrayList<>() : updated.getAboutSections()
        );

        SiteSettings existing = getSettings();

        existing.setFacebookUrl(updated.getFacebookUrl());
        existing.setPhone1(updated.getPhone1());
        existing.setPhone2(updated.getPhone2());
        existing.setAddress(updated.getAddress());
        existing.setRegCom(updated.getRegCom());
        existing.setCif(updated.getCif());

        existing.getAboutSections().clear();
        for (AboutSection sec : updated.getAboutSections()) {
            sec.setSiteSettings(existing);
            existing.getAboutSections().add(sec);
        }

        repository.save(existing);
    }
}
