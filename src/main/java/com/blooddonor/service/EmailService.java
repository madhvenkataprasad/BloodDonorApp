package com.blooddonor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendEmail(String to, String subject, String message) {

        String url = "https://api.brevo.com/v3/smtp/email";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        Map<String, Object> body = new HashMap<>();

        Map<String, String> sender = new HashMap<>();
        sender.put("name", senderName);
        sender.put("email", senderEmail);

        body.put("sender", sender);
        body.put("to", List.of(Map.of("email", to)));
        body.put("subject", subject);
        body.put("htmlContent", "<p>" + message + "</p>");

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, request, String.class);

            System.out.println("========================================");
            System.out.println("EMAIL SENT SUCCESSFULLY");
            System.out.println("To: " + to);
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}