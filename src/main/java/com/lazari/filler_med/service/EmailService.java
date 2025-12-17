package com.lazari.filler_med.service;

import com.lazari.filler_med.model.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.shipping.etaDays:2-4}")
    private String etaDays;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendShippedEmail(Order o) {
        if (o.getCustomerEmail() == null || o.getCustomerEmail().isBlank()) return;

        // minim de validare (nu perfect, dar ok)
        String email = o.getCustomerEmail().trim();
        if (!email.contains("@")) return;

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(email);
        msg.setSubject("Comanda #" + o.getId() + " a fost expediată ✅");

        String body = """
                Bună, %s!

                Comanda ta #%d a fost expediată.

                Adresă livrare:
                %s

                Estimare livrare: %s zile lucrătoare.

                Mulțumim,
                Fillermed
                """.formatted(
                safe(o.getCustomerName()),
                o.getId(),
                safe(o.getShippingAddress()),
                etaDays
        );

        msg.setText(body);
        mailSender.send(msg);
    }

    private String safe(String s) {
        return s == null ? "-" : s.trim();
    }
}
