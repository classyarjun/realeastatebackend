package com.RealEstateDevelopment.ServiceImpl;

import com.RealEstateDevelopment.CommanUtil.ValidationClass;
import com.RealEstateDevelopment.Entity.*;
import com.RealEstateDevelopment.Exception.UserNotFoundException;
import com.RealEstateDevelopment.Repository.ForgotPasswordOtpRepository;
import com.RealEstateDevelopment.Repository.TemporaryUserRepository;
import com.RealEstateDevelopment.Repository.UserRepository;
import com.RealEstateDevelopment.Service.EmailService;
import com.RealEstateDevelopment.Service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private ForgotPasswordOtpRepository otpRepository;

    @Autowired
    private TemporaryUserRepository temporaryUserRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private static final int OTP_EXPIRY_MINUTES = 5;

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    @Transactional
    public String registerTemporaryUser(User user) {
        validateUserData(user);

        // Validate password confirmation before encoding
        if (!user.getConfirmPassword().equals(user.getPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        String otp = generateOtp();

        TemporaryUser tempUser = new TemporaryUser();
        tempUser.setFullname(user.getFullname());
        tempUser.setEmail(user.getEmail());
        tempUser.setGender(user.getGender());
        tempUser.setMobileNo(user.getMobileNo());
        tempUser.setAddress(user.getAddress());
        tempUser.setUsername(user.getUsername());

        // Encode password before saving
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        tempUser.setPassword(encodedPassword);
        tempUser.setConfirmPassword(encodedPassword);

        tempUser.setOtp(otp);
        tempUser.setOtpExpiry(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)); // OTP expires in 5 minutes
        tempUser.setProfilePicture(user.getProfilePicture());
        tempUser.setStatus(Status.ACTIVE);
        tempUser.setRole(Role.USER);
        tempUser.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        tempUser.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        temporaryUserRepository.save(tempUser);

        emailService.sendEmail(user.getEmail(), "Your OTP Code to Register", "Your OTP code to register is: " + otp);

        return "Temporary user registered. Please verify OTP sent to your email.";
    }


    @Override
    @Transactional
    public String verifyOtpToRegister(String email, String otp) {
        logger.info("Attempting to verify OTP: {}", otp);
        Optional<TemporaryUser> tempUserOpt = temporaryUserRepository.findByOtp(otp);
        if (tempUserOpt.isEmpty()) {
            logger.error("OTP not found: {}", otp);
            throw new IllegalArgumentException("Temporary user not found");
        }

        TemporaryUser tempUser = tempUserOpt.get();
        logger.info("OTP found for email: {}", tempUser.getEmail());
        if (tempUser.getOtp().equals(otp) && tempUser.getOtpExpiry().isAfter(LocalDateTime.now())) {
            logger.info("OTP is valid and not expired");
            User user = new User();
            user.setFullname(tempUser.getFullname());
            user.setEmail(tempUser.getEmail());
            user.setGender(tempUser.getGender());
            user.setMobileNo(tempUser.getMobileNo());
            user.setAddress(tempUser.getAddress());
            user.setUsername(tempUser.getUsername());
            // Passwords are already encoded, so just copy them
            user.setPassword(tempUser.getPassword());
            user.setConfirmPassword(tempUser.getPassword());

            user.setProfilePicture(tempUser.getProfilePicture());
            user.setVerified(true);

            user.setStatus(Status.ACTIVE);
            user.setRole(Role.USER);
            user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            temporaryUserRepository.delete(tempUser);

            return "User verified and registered successfully.";
        } else {
            return "Invalid or expired OTP.";
        }
    }


    @Override
    public User loginUser(String username, String password) throws Exception {
        try {
            logger.info("Attempting to login user with username: {}", username);

            // Fetch user from the database
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();

                // Verify the password using PasswordEncoder
                if (passwordEncoder.matches(password, user.getPassword())) {
                    logger.info("User with username: {} logged in successfully.", username);
                    return user;
                }
            }

            // Log invalid login attempt
            logger.warn("Invalid login attempt for username: {}", username);
            throw new Exception("Invalid username or password");

        } catch (Exception e) {
            logger.error("Error during user login: {}", e.getMessage(), e);
            throw new Exception("Error during user login: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public User updateUserDetails(Long userId, User user) {
        try {
            logger.info("Updating user by ID: {}", userId);

            // Retrieve the existing user or throw an exception if not found
            User existingUser = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.warn("User with ID {} not found", userId);
                        return new UserNotFoundException("User with ID " + userId + " not found");
                    });

            // Update fields only if they are provided in the input user object
            if (user.getFullname() != null && !user.getFullname().isEmpty()) {
                existingUser.setFullname(user.getFullname());
            }
            if (user.getUsername() != null && !user.getUsername().isEmpty()) {
                existingUser.setUsername(user.getUsername());
            }
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                existingUser.setEmail(user.getEmail());
            }
            if (user.getGender() != null) {
                existingUser.setGender(user.getGender());
            }
            if (user.getMobileNo() != null && !user.getMobileNo().isEmpty()) {
                existingUser.setMobileNo(user.getMobileNo());
            }
            if (user.getAddress() != null && !user.getAddress().isEmpty()) {
                existingUser.setAddress(user.getAddress());
            }
            if (user.getProfilePicture() != null) {
                existingUser.setProfilePicture(user.getProfilePicture());
            }
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                // Encode the new password before saving
                existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            if (user.getConfirmPassword() != null && !user.getConfirmPassword().isEmpty()) {
                // Encode the confirmPassword as well
                existingUser.setConfirmPassword(passwordEncoder.encode(user.getConfirmPassword()));
            }

            // Update the timestamp
            existingUser.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

            logger.info("User details for ID {} have been updated: {}", userId, existingUser);

            // Save and return the updated user
            return userRepository.save(existingUser);

        } catch (UserNotFoundException e) {
            logger.error("Update failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while updating user with ID {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to update user details", e);
        }
    }



    @Override
    public void deleteUser(Long id) throws Exception {
        try {
            logger.info("Attempting to delete user with ID: {}", id);

            User user = userRepository.findById(id).orElseThrow(() -> new Exception("User not found"));
            userRepository.delete(user);
            logger.info("User with ID: {} deleted successfully.", id);
        } catch (Exception e) {
            logger.error("Error deleting user with ID: {}: {}", id, e.getMessage(), e);
            throw new Exception("Error deleting user: " + e.getMessage(), e);
        }
    }

    @Override
    public User getUserById(Long id) throws Exception {
        try {
            logger.info("Fetching user details with ID: {}", id);
            return userRepository.findById(id).orElseThrow(() -> new Exception("User not found"));
        } catch (Exception e) {
            logger.error("Error fetching user with ID: {}: {}", id, e.getMessage(), e);
            throw new Exception("Error fetching user: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> getAllUsers() {
        try {
            logger.info("Fetching all users.");
            return userRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching all users: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching all users: " + e.getMessage(), e);
        }
    }

    @Override
    public void changePassword(Long id, String oldPassword, String newPassword, String confirmPassword) throws Exception {
        try {
            logger.info("Attempting to change password for user with ID: {}", id);

            // Fetch user from the database
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new Exception("User not found"));

            // Validate old password with PasswordEncoder
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                logger.warn("Old password does not match for user with ID: {}", id);
                throw new Exception("Old password is incorrect");
            }

            // Validate new password and confirmation
            if (!newPassword.equals(confirmPassword)) {
                logger.warn("New password and confirm password do not match for user with ID: {}", id);
                throw new Exception("New password and confirm password must be the same");
            }

            // Encode new password before saving
            String encodedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedPassword);
            user.setConfirmPassword(encodedPassword);
            user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

            // Save updated user data
            userRepository.save(user);
            logger.info("Password changed successfully for user with ID: {}", id);
        } catch (Exception e) {
            logger.error("Error changing password for user with ID: {}: {}", id, e.getMessage(), e);
            throw new Exception("Error changing password: " + e.getMessage(), e);
        }
    }



    @Override
    public void logoutUser(String username) throws Exception {
        try {
            logger.info("Attempting to log out user with username: {}", username);
            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isEmpty()) {
                logger.warn("User not found for logout with username: {}", username);
                throw new Exception("User not found");
            }

            // Implement any required logout logic, e.g., token invalidation or session handling
            logger.info("User with username: {} logged out successfully.", username);
        } catch (Exception e) {
            logger.error("Error logging out user with username: {}: {}", username, e.getMessage(), e);
            throw new Exception("Error logging out user: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public String deleteProfilePicture(Long userId) {
        try {
            logger.info("Attempting to delete profile picture for user ID: {}", userId);
            // Retrieve the user by ID
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.warn("User with ID {} not found :", userId);
                        return new UserNotFoundException("User with ID " + userId + " not found");
                    });
            // Remove the profile picture
            user.setProfilePicture(null);
            userRepository.save(user);

            logger.info("Profile picture deleted successfully for user ID: {}", userId);

            return "Profile picture deleted successfully.";

        } catch (UserNotFoundException e) {
            logger.error("Failed to delete profile picture: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while deleting profile picture for user ID {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete profile picture", e);
        }
    }


    private String generateOtp() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000)); // generate 6-digit OTP
    }


    private void validateUserData(User user) throws IllegalArgumentException {
        if (user.getUsername() == null || !ValidationClass.USERNAME_PATTERN.matcher(user.getUsername()).matches()) {
            throw new IllegalArgumentException("Username is required and should be alphanumeric.");
        }
        if (user.getEmail() == null || !ValidationClass.EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            throw new IllegalArgumentException("Email is not valid.");
        }
        if (user.getGender() == null || !ValidationClass.GENDER_PATTERN.matcher(user.getGender()).matches()) {
            throw new IllegalArgumentException("Gender is required and must be Male, Female, or Other.");
        }
        if (user.getMobileNo() == null || !ValidationClass.PHONE_PATTERN.matcher(user.getMobileNo()).matches()) {
            throw new IllegalArgumentException("Mobile number should be 10 digits.");
        }
        if (user.getAddress() == null || !ValidationClass.ADDRESS_PATTERN.matcher(user.getAddress()).matches()) {
            throw new IllegalArgumentException("Address is required and contains invalid characters.");
        }
        if (user.getPassword() == null || !ValidationClass.PASSWORD_PATTERN.matcher(user.getPassword()).matches()) {
            throw new IllegalArgumentException("Password should be 6-20 characters, including at least one letter, one number, and one special character.");
        }
    }
}
