package com.lazari.filler_med.repostiory;

import com.lazari.filler_med.model.SiteSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteSettingsRepository extends JpaRepository<SiteSettings, Long> {
}
