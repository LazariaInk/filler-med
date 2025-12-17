package com.lazari.filler_med.advice;

import com.lazari.filler_med.service.AdminOrderQueueService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class AdminGlobalModel {

    private final AdminOrderQueueService queue;

    public AdminGlobalModel(AdminOrderQueueService queue) {
        this.queue = queue;
    }

    @ModelAttribute("toFulfillCount")
    public long toFulfillCount() {
        return queue.getToFulfillCount();
    }
}
