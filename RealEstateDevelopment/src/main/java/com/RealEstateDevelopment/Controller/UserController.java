package com.RealEstateDevelopment.Controller;

import com.RealEstateDevelopment.Entity.User;
import com.RealEstateDevelopment.Exception.UserNotFoundException;
import com.RealEstateDevelopment.Repository.ForgotPasswordOtpRepository;
import com.RealEstateDevelopment.Service.EmailService;
import com.RealEstateDevelopment.Service.UserService;
import com.RealEstateDevelopment.ServiceImpl.ForgotPasswordService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ForgotPasswordOtpRepository otpRepository;

    @Autowired
    private ForgotPasswordService forgotPasswordService;

//    @PostMapping("/registerTemporaryUser")
//    public ResponseEntity<String> registerTemporaryUser(@RequestPart("userData") String userData,
//                                               @RequestPart("profilePicture") MultipartFile multipartFile) throws JsonProcessingException {
//        ObjectMapper objectMapper = new ObjectMapper();
//        User user = objectMapper.readValue(userData, User.class);
//        try {
//            if (multipartFile != null && !multipartFile.isEmpty()) {
//                String contentType = multipartFile.getContentType();
//                if (contentType == null || isValidImageType(contentType)) {
//                    return ResponseEntity.badRequest().body("Invalid profile picture format. Only JPEG and PNG and png are supported.");
//                }
//                user.setProfilePicture(multipartFile.getBytes());
//            } else {
//                user.setProfilePicture(null);
//            }
//            String message = userService.registerTemporaryUser(user);
//            return ResponseEntity.ok(message);
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
//        }
//    }

    @PostMapping("/registerTemporaryUser")
    public ResponseEntity<Map<String, String>> registerTemporaryUser(@RequestPart("userData") String userData,
                                                                     @RequestPart("profilePicture") MultipartFile multipartFile) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> userDataMap = objectMapper.readValue(userData, Map.class);
        User user = objectMapper.readValue(userData, User.class);
        try {    if (multipartFile != null && !multipartFile.isEmpty())
        {      String contentType = multipartFile.getContentType();
            if (contentType == null || !isValidImageType(contentType))
            {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid profile picture format. Only JPEG and PNG are supported."));
            }      user.setProfilePicture(multipartFile.getBytes());    } else {      user.setProfilePicture(null);    }
            String message = userService.registerTemporaryUser(user);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "An unexpected error occurred: " + e.getMessage()));
        }}
    @PostMapping("/verifyOtpToRegisterUser")
    public ResponseEntity<String> verifyUserOtpToRegisterUser(@RequestParam String email, @RequestParam String otp) {
        try {
            String message = userService.verifyOtpToRegister(email, otp);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/loginUser")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginDetails) {
        try {
            logger.info("User login attempt...");
            String username = loginDetails.get("username");
            String password = loginDetails.get("password");

            if (username == null || password == null) {
                logger.warn("Username or password is missing.");
                return ResponseEntity.badRequest().body("Username and password are required.");
            }

            User user = userService.loginUser(username, password);
            logger.info("User logged in successfully: {}", username);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Error during login: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error during login: " + e.getMessage());
        }
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable Long userId,
                                           @RequestPart("userData") String userData,
                                           @RequestPart(value = "profilePicture", required = false) MultipartFile multipartFile) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            User user = objectMapper.readValue(userData, User.class);
            // If a profile picture is provided, validate and set it
            if (multipartFile != null && !multipartFile.isEmpty()) {
                String contentType = multipartFile.getContentType();
                if (contentType == null || isValidImageType(contentType)) {
                    return ResponseEntity.badRequest().body(null);
                }
                user.setProfilePicture(multipartFile.getBytes());
            } else {
                User existingUser = userService.getUserById(userId);
                // If no new profile picture is provided, keep the existing one
                user.setProfilePicture(existingUser.getProfilePicture());
            }
            // Update user details
            User updatedUser = userService.updateUserDetails(userId, user);
            return ResponseEntity.ok(updatedUser);

        } catch (JsonProcessingException e) {
            logger.error("Failed to parse user data: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("An error occurred while updating user with ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/deleteUser/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            logger.info("Attempting to delete user with ID: {}", userId);
            userService.deleteUser(userId);
            logger.info("User with ID: {} deleted successfully.", userId);

            response.put("status", 200);
            response.put("message", "User deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error during user deletion for ID {}: {}", userId, e.getMessage(), e);

            response.put("status", 400);
            response.put("message", "Error during user deletion: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/getUserById/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            if (userId == null) {
                logger.warn("User ID cannot be null");
                return ResponseEntity.badRequest().body("User ID cannot be null");
            }
            // Retrieve the user
            User user = userService.getUserById(userId);
            // Return the retrieved user
            return ResponseEntity.ok(user);

        } catch (UserNotFoundException e) {
            logger.warn("User not found with ID: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with ID: " + userId);
        } catch (Exception e) {
            logger.error("An unexpected error occurred while retrieving user with ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/getAllUsers")
    public ResponseEntity<?> getAllUsers() {
        try {
            logger.info("Fetching all users.");
            List<User> users = userService.getAllUsers();  // This should return List<User>
            logger.info("Retrieved {} users.", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error fetching all users: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error fetching users: " + e.getMessage());
        }
    }


    @PutMapping("/changePassword/{userId}")
    public ResponseEntity<?> changePassword(@PathVariable Long userId,
                                            @RequestParam String oldPassword,
                                            @RequestParam String newPassword,
                                            @RequestParam String confirmPassword) {
        try {
            logger.info("Changing password for user ID: {}", userId);
            userService.changePassword(userId, oldPassword, newPassword, confirmPassword);
            logger.info("Password updated successfully for user ID: {}", userId);
            return ResponseEntity.ok("Password updated successfully");
        } catch (Exception e) {
            logger.error("Error changing password for user ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error changing password: " + e.getMessage());
        }
    }


    @PostMapping("/logoutUser")
    public ResponseEntity<?> logoutUser(@RequestParam String username) {
        try {
            logger.info("User logout attempt for username: {}", username);
            userService.logoutUser(username);
            logger.info("User {} logged out successfully.", username);
            return ResponseEntity.ok("Logout successful");
        } catch (Exception e) {
            logger.error("Error during logout for user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error during logout: " + e.getMessage());
        }
    }

    @DeleteMapping("/user/profilePicture/delete/{userId}")
    public ResponseEntity<String> deleteProfilePicture(@PathVariable Long userId) {
        try {
            logger.info("Received request to delete profile picture for user ID: {}", userId);
            // Call the service method to delete the profile picture
            String message = userService.deleteProfilePicture(userId);
            // Return a success response
            return ResponseEntity.ok(message);

        } catch (UserNotFoundException e) {
            logger.warn("Profile picture deletion failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("An unexpected error occurred: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }


        private boolean isValidImageType(String contentType) {
        return contentType.equalsIgnoreCase("image/jpeg")
                || contentType.equalsIgnoreCase("image/png");
    }

}
