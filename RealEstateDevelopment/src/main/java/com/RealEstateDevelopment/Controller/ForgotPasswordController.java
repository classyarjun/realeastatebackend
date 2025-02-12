package com.RealEstateDevelopment.Controller;

import com.RealEstateDevelopment.Entity.PasswordResetRequest;
import com.RealEstateDevelopment.Exception.AdminNotFoundException;
import com.RealEstateDevelopment.Exception.AgentNotFoundException;
import com.RealEstateDevelopment.Exception.UserNotFoundException;
import com.RealEstateDevelopment.ServiceImpl.ForgotPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/forgotPassword")
@CrossOrigin("*")
public class ForgotPasswordController {

    private static final Logger logger = LoggerFactory.getLogger(ForgotPasswordController.class);
    private final ForgotPasswordService forgotPasswordService;

    public ForgotPasswordController(ForgotPasswordService forgotPasswordService) {
        this.forgotPasswordService = forgotPasswordService;
    }

    // ======================= USER PASSWORD RESET =======================

    @PostMapping("/user/{email}")
    public ResponseEntity<String> forgotPasswordUser(@PathVariable String email) {
        return handleForgotPassword(email, "User");
    }

    @PostMapping("/user/resetPassword/{email}")
    public ResponseEntity<String> resetPasswordUser(@PathVariable String email,
                                                    @RequestBody PasswordResetRequest request) {
        return handleResetPassword(email, request, "User");
    }

    // ======================= ADMIN PASSWORD RESET =======================

    @PostMapping("/admin/{email}")
    public ResponseEntity<String> forgotPasswordAdmin(@PathVariable String email) {
        return handleForgotPassword(email, "Admin");
    }

    @PostMapping("/admin/resetPassword/{email}")
    public ResponseEntity<String> resetPasswordAdmin(@PathVariable String email,
                                                     @RequestBody PasswordResetRequest request) {
        return handleResetPassword(email, request, "Admin");
    }

    // ======================= AGENT PASSWORD RESET =======================

    @PostMapping("/agent/{email}")
    public ResponseEntity<String> forgotPasswordAgent(@PathVariable String email) {
        return handleForgotPassword(email, "Agent");
    }

    @PostMapping("/agent/resetPassword/{email}")
    public ResponseEntity<String> resetPasswordAgent(@PathVariable String email,
                                                     @RequestBody PasswordResetRequest request) {
        return handleResetPassword(email, request, "Agent");
    }

    // ======================= COMMON HELPER METHODS =======================

    private ResponseEntity<String> handleForgotPassword(String email, String entityType) {
        logger.info("Received password reset request for {} with email: {}", entityType, email);
        try {
            forgotPasswordService.requestPasswordReset(email);
            return ResponseEntity.ok(entityType + " password reset OTP has been sent to your email.");
        } catch (UserNotFoundException | AdminNotFoundException | AgentNotFoundException e) {
            logger.error("{} not found with email: {}", entityType, email);
            return ResponseEntity.status(404).body(entityType + " not found with email: " + email);
        } catch (Exception e) {
            logger.error("An error occurred while processing the password reset request for {} with email: {}", entityType, email, e);
            return ResponseEntity.status(500).body("An unexpected error occurred.");
        }
    }

    private ResponseEntity<String> handleResetPassword(String email, PasswordResetRequest request, String entityType) {
        String password = request.getPassword();
        String confirmPassword = request.getConfirmPassword();
        String otp = request.getOtp();

        logger.info("Received request to reset {} password for email: {}", entityType, email);

        // Step 1: Check if passwords match
        if (!password.equals(confirmPassword)) {
            logger.error("{} password reset failed: passwords do not match for email: {}", entityType, email);
            return ResponseEntity.status(400).body("Passwords do not match");
        }

        try {
            // Step 2: Verify OTP and reset password
            boolean isOtpValid = forgotPasswordService.verifyOtp(otp, email);

            if (!isOtpValid) {
                logger.error("Invalid or expired OTP: {} for {} email: {}", otp, entityType, email);
                return ResponseEntity.status(400).body("Invalid or expired OTP");
            }

            forgotPasswordService.resetPassword(email, password);
            logger.info("{} password successfully reset for email: {}", entityType, email);
            return ResponseEntity.ok(entityType + " password successfully reset");
        } catch (UserNotFoundException | AdminNotFoundException | AgentNotFoundException e) {
            logger.error("No {} account found for email: {}", entityType, email, e);
            return ResponseEntity.status(404).body("No " + entityType + " account found for email: " + email);
        } catch (Exception e) {
            logger.error("An error occurred while resetting {} password for email: {}", entityType, email, e);
            return ResponseEntity.status(500).body("An unexpected error occurred.");
        }
    }
}
