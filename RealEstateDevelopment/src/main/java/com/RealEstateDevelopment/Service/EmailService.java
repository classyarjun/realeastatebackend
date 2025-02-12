package com.RealEstateDevelopment.Service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);

    // New overloaded method for sending property approval emails (from agent to admin)
    void sendEmail(String from, String to, String subject, String body);
}