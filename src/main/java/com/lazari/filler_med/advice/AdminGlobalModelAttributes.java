package com.lazari.filler_med.advice;

import com.lazari.filler_med.service.AdminBadgeService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class AdminGlobalModelAttributes {

    private final AdminBadgeService badgeService;

    public AdminGlobalModelAttributes(AdminBadgeService badgeService) {
        this.badgeService = badgeService;
    }

    @ModelAttribute("openOrdersCount")
    public long openOrdersCount() {
        return badgeService.openOrdersCount();
    }
}
