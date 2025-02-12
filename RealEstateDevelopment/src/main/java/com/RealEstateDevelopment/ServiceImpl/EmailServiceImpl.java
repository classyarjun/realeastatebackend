package com.RealEstateDevelopment.ServiceImpl;

import com.RealEstateDevelopment.Service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailServiceImpl implements EmailService {

    private final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender emailSender;

    @Override
    public void sendEmail(String to, String subject, String body) {

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom("anoreply079@gmail.com");
            message.setSubject(subject);
            message.setText(body);
            emailSender.send(message);
            logger.info("Email sent to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send email to {}", to, e);
        }
    }


    // Overloaded method to send an email from the agent's email to the admin for property approval.

    public void sendEmail(String from, String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from); // Set the agent's email as the sender
            message.setTo(to);     // Admin's email as the recipient
            message.setSubject(subject);
            message.setText(body);
            emailSender.send(message);
            logger.info("Email sent to admin from {} for property approval.", from);
        } catch (Exception e) {
            logger.error("Failed to send property approval email from {} to {}", from, to, e);
        }
    }

}