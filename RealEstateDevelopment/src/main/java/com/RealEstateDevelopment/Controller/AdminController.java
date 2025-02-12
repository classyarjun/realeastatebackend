package com.RealEstateDevelopment.Controller;

import com.RealEstateDevelopment.Entity.Admin;
import com.RealEstateDevelopment.Entity.Agent;
import com.RealEstateDevelopment.Entity.PendingProperty;
import com.RealEstateDevelopment.Entity.PropertyNew;
import com.RealEstateDevelopment.Exception.UserNotFoundException;
import com.RealEstateDevelopment.Exceptions.PropertyNotFoundException;
import com.RealEstateDevelopment.Repository.ForgotPasswordOtpRepository;
import com.RealEstateDevelopment.Service.AdminService;
import com.RealEstateDevelopment.Service.AgentService;
import com.RealEstateDevelopment.Service.EmailService;
import com.RealEstateDevelopment.Service.PropertyNewService;
import com.RealEstateDevelopment.ServiceImpl.ForgotPasswordService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin("*")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminService adminService;

    @Autowired
    private ForgotPasswordOtpRepository otpRepository;  // Repository for OTPs

    @Autowired
    private EmailService emailService;

    @Autowired
    private PropertyNewService propertyNewService;

    @Autowired
    private AgentService agentService;

    @Autowired
    private ForgotPasswordService forgotPasswordService;


//    @PostMapping("/registerAdmin")
//    public ResponseEntity<String> registerAdmin(
//            @RequestParam("adminData") String adminData,
//            @RequestParam(value = "profilePicture", required = false) MultipartFile multipartFile) {
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            // Parse the admin data from JSON
//            Admin admin = objectMapper.readValue(adminData, Admin.class);
//
//            // Handle profile picture upload if provided
//            if (multipartFile != null && !multipartFile.isEmpty()) {
//                String contentType = multipartFile.getContentType();
//                if (contentType != null && isValidImageType(contentType)) {
//                    admin.setProfilePicture(multipartFile.getBytes());
//                } else {
//                    return ResponseEntity.badRequest().body("Invalid profile picture format. Only JPEG and PNG are supported.");
//                }
//            } else {
//                admin.setProfilePicture(null);  // No profile picture, setting to null
//            }
//
//            // Register the admin
//            adminService.registerAdmin(admin);
//            return ResponseEntity.status(HttpStatus.OK).body("Successfully registered Admin");
//        } catch (JsonProcessingException e) {
//            logger.error("Error parsing admin data: {}", e.getMessage());
//            return ResponseEntity.badRequest().body("Invalid admin data format.");
//        } catch (IOException e) {
//            logger.error("Error processing profile picture: {}", e.getMessage());
//            return ResponseEntity.badRequest().body("Error processing the profile picture.");
//        } catch (IllegalArgumentException e) {
//            logger.error("Error during registration: {}", e.getMessage());
//            return ResponseEntity.badRequest().body(e.getMessage());
//        } catch (Exception e) {
//            logger.error("An unexpected error occurred: {}", e.getMessage());
//            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
//        }
//    }

    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/registerAdmin")
    public ResponseEntity<Map<String, String>> registerAdmin(
            @RequestParam("adminData") String adminData,
            @RequestParam(value = "profilePicture", required = false) MultipartFile multipartFile) {

        Map<String, String> response = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Parse the admin data from JSON
            Admin admin = objectMapper.readValue(adminData, Admin.class);

            // Handle profile picture upload if provided
            if (multipartFile != null && !multipartFile.isEmpty()) {
                String contentType = multipartFile.getContentType();
                if (contentType != null && isValidImageType(contentType)) {
                    admin.setProfilePicture(multipartFile.getBytes());
                } else {
                    response.put("message", "Invalid profile picture format. Only JPEG and PNG are supported.");
                    return ResponseEntity.badRequest().body(response);
                }
            } else {
                admin.setProfilePicture(null); // No profile picture, setting to null
            }

            // Register the admin
            adminService.registerAdmin(admin);
            response.put("message", "Successfully registered Admin");
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (JsonProcessingException e) {
            logger.error("Error parsing admin data: {}", e.getMessage());
            response.put("message", "Invalid admin data format.");
            return ResponseEntity.badRequest().body(response);
        } catch (IOException e) {
            logger.error("Error processing profile picture: {}", e.getMessage());
            response.put("message", "Error processing the profile picture.");
            return ResponseEntity.badRequest().body(response);
        } catch (IllegalArgumentException e) {
            logger.error("Error during registration: {}", e.getMessage());
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("An unexpected error occurred: {}", e.getMessage());
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    // Helper method to validate image types
    private boolean isValidImageType(String contentType) {
        return contentType.equalsIgnoreCase("image/jpeg") ||
                contentType.equalsIgnoreCase("image/png") ||
                contentType.equalsIgnoreCase("image/jpg");
    }

    @PostMapping("/loginAdmin")
    public ResponseEntity<Map<String, Object>> loginAdmin(@RequestParam String username, @RequestParam String password) {
        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("Login attempt for username: {}", username);
            String result = adminService.loginAdmin(username, password);

            response.put("status", 200);
            response.put("message", "Admin Login Successfully");
            return ResponseEntity.ok(response);

        } catch (UserNotFoundException e) {
            logger.error("Error occurred during login: {}", e.getMessage());
            response.put("status", 404);
            response.put("message", "Admin not found with username: " + username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (RuntimeException e) {
            logger.error("Invalid credentials for username: {}", username);
            response.put("status", 401);
            response.put("message", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);

        } catch (Exception e) {
            logger.error("An unexpected error occurred: {}", e.getMessage());
            response.put("status", 500);
            response.put("message", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @DeleteMapping("/deleteAdmin/{adminId}")
    public ResponseEntity<Map<String, String>> deleteAdmin(@PathVariable Long adminId) {

        Map<String, String> response = new HashMap<>();

        try {
            if (adminId == null) {
                throw new UserNotFoundException("Admin ID cannot be null");
            }

            adminService.deleteAdmin(adminId);
            response.put("message", "Successfully deleted admin");
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (UserNotFoundException e) {
            response.put("message", "Admin not found with ID: " + adminId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PostMapping("/logoutAdmin")
    public ResponseEntity<String> logoutAdmin(@RequestParam String username) {
        try {
            logger.info("Logout attempt for username: {}", username);
            adminService.logoutAdmin(username);
            return ResponseEntity.status(HttpStatus.OK).body("Admin logged out successfully.");
        } catch (UserNotFoundException e) {
            logger.error("Error occurred during logout: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Admin not found with username: " + username);
        } catch (Exception e) {
            logger.error("An unexpected error occurred: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }


    @GetMapping("/getAdminById/{adminId}")
    public ResponseEntity<?> getAdminById(@PathVariable Long adminId) {
        try {
            if (adminId == null) {
                throw new IllegalArgumentException("Admin ID cannot be null.");
            }
            Admin admin = adminService.getAdminById(adminId);
            return ResponseEntity.ok(admin);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Admin not found with ID: " + adminId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/getAdminByUsername/{username}")
    public ResponseEntity<?> getAdminByUsername(@PathVariable String username) {
        try {
            if (username == null || username.isEmpty()) {
                throw new IllegalArgumentException("Username cannot be null or empty.");
            }
            Admin admin = adminService.getAdminByUsername(username);
            return ResponseEntity.ok(admin);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Admin not found with username: " + username);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/getAllAdmins")
    public ResponseEntity<?> getAllAdmins() {
        try {
            return ResponseEntity.ok(adminService.getAllAdmins());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PutMapping("/updateAdmin/{adminId}")
    public ResponseEntity<String> updateAdmin(
            @PathVariable Long adminId,
            @RequestParam(value = "adminData") String adminData,
            @RequestParam(value = "profilePicture", required = false) MultipartFile multipartFile) {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Parse the admin data from JSON
            Admin updatedAdmin = objectMapper.readValue(adminData, Admin.class);

            // Set the admin ID to ensure the correct admin is updated
            updatedAdmin.setAdminId(adminId);

            // If profile picture is uploaded, set it in the Admin entity
            if (multipartFile != null && !multipartFile.isEmpty()) {
                String contentType = multipartFile.getContentType();
                if (contentType != null && isValidImageType(contentType)) {
                    updatedAdmin.setProfilePicture(multipartFile.getBytes());
                } else {
                    return ResponseEntity.badRequest().body("Invalid profile picture format. Only JPEG and PNG are supported.");
                }
            }

            // Call the service to update the admin data
            String result = adminService.updateAdmin(adminId, updatedAdmin);

            // Return the result of the update operation
            return ResponseEntity.status(HttpStatus.OK).body(result);

        } catch (JsonProcessingException e) {
            logger.error("Error parsing admin data: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid admin data format.");
        } catch (IOException e) {
            logger.error("Error processing profile picture: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error processing the profile picture.");
        } catch (IllegalArgumentException e) {
            logger.error("Error during update: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("An unexpected error occurred: {}", e.getMessage());
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Approve an agent by admin.
     */
    @PostMapping("/approveAgent/{tempAgentId}")
    public ResponseEntity<?> approveAgent(@PathVariable Long tempAgentId) {
        try {
            logger.info("Approving agent with temporary ID: {}", tempAgentId);
            Agent approvedAgent = agentService.approveAgent(tempAgentId);
            logger.info("Agent approved successfully with ID: {}", approvedAgent.getId());
            return ResponseEntity.ok(approvedAgent);
        } catch (Exception e) {
            logger.error("Error approving agent: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error approving agent: " + e.getMessage());
        }
    }

    /**
     * Reject an agent by admin.
     */
    @DeleteMapping("/rejectAgent/{tempAgentId}")
    public ResponseEntity<?> rejectAgent(@PathVariable Long tempAgentId) {
        try {
            logger.info("Rejecting agent with temporary ID: {}", tempAgentId);
            agentService.rejectAgent(tempAgentId);
            logger.info("Agent rejected successfully.");
            return ResponseEntity.ok("Agent rejected successfully.");
        } catch (Exception e) {
            logger.error("Error rejecting agent: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error rejecting agent: " + e.getMessage());
        }
    }

    // Approve Property by admin

    @PutMapping("/approveProperty/{propertyId}")
    public ResponseEntity<Map<String, String>> approveProperty(@PathVariable Long propertyId) {
        Map<String, String> response = new HashMap<>();

        try {
            propertyNewService.approveProperty(propertyId);
            response.put("message", "Property approved successfully");
            return ResponseEntity.ok(response);
        } catch (PropertyNotFoundException e) {
            logger.error("Property not found with ID: {}", propertyId);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            logger.error("Error approving property with ID {}: {}", propertyId, e.getMessage());
            response.put("message", "Error approving property: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Reject Property by admin

    @DeleteMapping("/rejectProperty/{propertyId}")
    public ResponseEntity<Map<String, String>> rejectProperty(@PathVariable Long propertyId) {
        Map<String, String> response = new HashMap<>();

        try {
            logger.info("Rejecting and deleting property with ID: {}", propertyId);
            propertyNewService.rejectProperty(propertyId);
            logger.info("Property with ID {} rejected and deleted successfully", propertyId);

            response.put("message", "Property rejected and deleted successfully");
            return ResponseEntity.ok(response);
        } catch (PropertyNotFoundException e) {
            logger.error("Property not found with ID: {}", propertyId);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            logger.error("Error rejecting and deleting property with ID {}: {}", propertyId, e.getMessage());
            response.put("message", "Error rejecting and deleting property: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Get All Pending Properties

    @GetMapping("/getAllPendingProperties")
    public ResponseEntity<List<PendingProperty>> getAllPendingProperties() {
        try {
            logger.info("Fetching all pending properties");
            List<PendingProperty> pendingProperties = propertyNewService.getAllPendingProperties();
            if (pendingProperties.isEmpty()) {
                logger.warn("No pending properties found");
                return ResponseEntity.noContent().build();  // Return 204 No Content if no pending properties exist
            }
            logger.info("Fetched {} pending properties", pendingProperties.size());
            return ResponseEntity.ok(pendingProperties);
        } catch (Exception e) {
            logger.error("Error fetching pending properties: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    // Update Property with agent and properties

    @PutMapping("/updateAgentAndProperty/{propertyId}")
    public ResponseEntity<?> updateAgentAndProperty(@PathVariable Long propertyId,
                                            @RequestPart("property") PropertyNew updatedProperty,
                                            @RequestPart(value = "images", required = false) List<MultipartFile> newImages) {
        try {
            logger.info("Updating property with ID: {}", propertyId);
            PropertyNew updated = propertyNewService.updateProperty(propertyId, updatedProperty, newImages);
            logger.info("Property with ID {} updated successfully", propertyId);
            return ResponseEntity.ok(updated);  // Return updated property
        } catch (Exception e) {
            logger.error("Error updating property with ID {}: {}", propertyId, e.getMessage());
            return ResponseEntity.status(500).body("Error updating property: " + e.getMessage());
        }
    }

}
