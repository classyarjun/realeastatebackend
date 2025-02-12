package com.RealEstateDevelopment.ServiceImpl;

import com.RealEstateDevelopment.Controller.AdminController;
import com.RealEstateDevelopment.Entity.Admin;
import com.RealEstateDevelopment.Entity.Agent;
import com.RealEstateDevelopment.Entity.ForgotPasswordOtp;
import com.RealEstateDevelopment.Entity.User;
import com.RealEstateDevelopment.Exception.AdminNotFoundException;
import com.RealEstateDevelopment.Exception.UserNotFoundException;
import com.RealEstateDevelopment.Repository.AdminRepository;
import com.RealEstateDevelopment.Repository.AgentRepository;
import com.RealEstateDevelopment.Repository.ForgotPasswordOtpRepository;
import com.RealEstateDevelopment.Repository.UserRepository;
import com.RealEstateDevelopment.Service.EmailService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class ForgotPasswordService {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private ForgotPasswordOtpRepository otpRepository;

    @Autowired
    private EmailService emailService;

    private final PasswordEncoder passwordEncoder;

    private static final int OTP_EXPIRY_MINUTES = 5;  // OTP expiration time in minutes

    public ForgotPasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String requestPasswordReset(String email) {
        // Check if email belongs to Admin
        Admin admin = adminRepository.findByEmail(email);
        if (admin != null) {
            return generateOtpForAdmin(admin);
        }

        // Check if email belongs to User
        User user = userRepository.findByEmail(email);
        if (user != null) {
            return generateOtpForUser(user);
        }

        // Check if email belongs to Agent
        Agent agent = agentRepository.findByEmail(email);
        if (agent != null) {
            return generateOtpForAgent(agent);
        }

        throw new AdminNotFoundException("No Admin, User, or Agent found with this email: " + email);
    }


    // Generate OTP for User
    public String generateOtpForUser(User user) {
        return generateAndSendOtp(user.getEmail(), user, null, null);
    }

    // Generate OTP for Admin
    public String generateOtpForAdmin(Admin admin) {
        return generateAndSendOtp(admin.getEmail(), null, admin, null);
    }

    // Generate OTP for Agent
    public String generateOtpForAgent(Agent agent) {
        return generateAndSendOtp(agent.getEmail(), null, null, agent);
    }


    private String generateAndSendOtp(String email, User user, Admin admin, Agent agent) {
        // Check if an OTP already exists for this user/admin/agent
        ForgotPasswordOtp existingOtp = otpRepository.findByUserOrAdminOrAgent(user, admin, agent);

        // If an existing OTP is found, delete it (or update its expiry time if needed)
        if (existingOtp != null) {
            otpRepository.delete(existingOtp); // Or reset its expiry time and OTP if needed
        }

        // Generate new OTP
        String otp = generateOtp();
        ForgotPasswordOtp otpEntity = new ForgotPasswordOtp();
        otpEntity.setOtp(otp);
        otpEntity.setCreatedAt(LocalDateTime.now());
        otpEntity.setExpiryDate(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));

        // Associate with the correct entity (user, admin, or agent)
        if (user != null) {
            otpEntity.setUser(user);  // Associate with User
        } else if (admin != null) {
            otpEntity.setAdmin(admin);  // Associate with Admin
        } else if (agent != null) {
            otpEntity.setAgent(agent);  // Associate with Agent
        }

        // Save OTP entity
        otpRepository.save(otpEntity);

        // Send OTP email
        emailService.sendEmail(email, "Password Reset OTP", "Your OTP for password reset is: " + otp);

        return otp;
    }


    // Helper method to generate OTP
    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));  // Ensures 6-digit OTP
    }

    // Verify OTP for Admin, User, or Agent
    public boolean verifyOtp(String otp, String email) {
        Admin admin = adminRepository.findByEmail(email);
        if (admin != null) {
            return verifyOtpForAdmin(otp, admin);
        }

        User user = userRepository.findByEmail(email);
        if (user != null) {
            return verifyOtpForUser(otp, user);
        }

        Agent agent = agentRepository.findByEmail(email);
        if (agent != null) {
            return verifyOtpForAgent(otp, agent);
        }

        throw new AdminNotFoundException("No Admin, User, or Agent found with this email: " + email);
    }

    // Verify OTP for Admin
    private boolean verifyOtpForAdmin(String otp, Admin admin) {
        return verifyOtpEntity(otp, admin.getEmail(), null, admin, null);
    }

    // Verify OTP for User
    private boolean verifyOtpForUser(String otp, User user) {
        return verifyOtpEntity(otp, user.getEmail(), user, null, null);
    }

    // Verify OTP for Agent
    private boolean verifyOtpForAgent(String otp, Agent agent) {
        return verifyOtpEntity(otp, agent.getEmail(), null, null, agent);
    }

    // Unified method to verify OTP
    private boolean verifyOtpEntity(String otp, String email, User user, Admin admin, Agent agent) {
        ForgotPasswordOtp otpEntity = otpRepository.findByOtp(otp)
                .filter(record -> record.getUser() == user && record.getAdmin() == admin && record.getAgent() == agent)
                .orElseThrow(() -> new IllegalArgumentException("Invalid OTP"));

        if (otpEntity.getCreatedAt().plusMinutes(OTP_EXPIRY_MINUTES).isBefore(LocalDateTime.now())) {
            otpRepository.delete(otpEntity);
            return false;
        }

        return true;
    }

    // Reset password for Admin, User, or Agent
    @Transactional
    public void resetPassword(String email, String newPassword) {
        logger.info("Initiating password reset for email: {}", email);

        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password cannot be null or empty.");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        logger.debug("Encoded new password for email {}: {}", email, encodedPassword);

        boolean isPasswordReset = false;

        // Reset password for Admin
        Admin admin = adminRepository.findByEmail(email);
        if (admin != null) {
            admin.setPassword(encodedPassword);
            adminRepository.save(admin);
            isPasswordReset = true;
        }

        // Reset password for User
        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.setPassword(encodedPassword);
            userRepository.save(user);
            isPasswordReset = true;
        }

        // Reset password for Agent
        Agent agent = agentRepository.findByEmail(email);
        if (agent != null) {
            agent.setPassword(encodedPassword);
            agentRepository.save(agent);
            isPasswordReset = true;
        }

        if (!isPasswordReset) {
            logger.error("No Admin, User, or Agent account found for email: {}", email);
            throw new UserNotFoundException("No Admin, User, or Agent found with this email: " + email);
        }

        logger.info("Password reset process completed for email: {}", email);
    }
}
