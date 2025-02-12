package com.RealEstateDevelopment.ServiceImpl;

import com.RealEstateDevelopment.CommanUtil.ValidationClass;
import com.RealEstateDevelopment.Entity.*;
import com.RealEstateDevelopment.Exception.AgentNotFoundException;
import com.RealEstateDevelopment.Exception.UserNotFoundException;
import com.RealEstateDevelopment.Repository.AdminRepository;
import com.RealEstateDevelopment.Repository.AgentRepository;
import com.RealEstateDevelopment.Repository.PropertyNewRepository;
import com.RealEstateDevelopment.Service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PropertyNewRepository propertyRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public String registerAdmin(Admin admin) {
        logger.info("Registering admin with username: {}", admin.getUsername());
        validateUserData(admin);

        if (adminRepository.existsByUsername(admin.getUsername()) ||
                adminRepository.existsByEmail(admin.getEmail()) ||
                adminRepository.existsByMobileNo(admin.getMobileNo())) {
            logger.error("Admin already exists with username: {}, email: {}, or mobileNo: {}",
                    admin.getUsername(), admin.getEmail(), admin.getMobileNo());
            throw new RuntimeException("Admin with the given username, email, or mobile number already exists.");
        }

        admin.setRole(Role.ADMIN);
        admin.setStatus(Status.ACTIVE);
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));

        if (admin.getProfilePicture() == null || admin.getProfilePicture().length == 0) {
            admin.setProfilePicture(null); // Set to null if not provided
            logger.info("No profile picture uploaded for admin.");
        } else {
            logger.info("Profile picture uploaded for admin.");
        }

        Admin savedAdmin = adminRepository.save(admin);
        logger.info("Successfully registered admin with ID: {}", savedAdmin.getAdminId());
        return "Admin registered successfully.";
    }

    @Override
    public String loginAdmin(String username, String password) {
        logger.info("Admin login attempt for username: {}", username);
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Admin not found with username: {}", username);
                    return new UserNotFoundException("Admin not found");
                });

        if (!passwordEncoder.matches(password, admin.getPassword())) {
            logger.error("Invalid password for username: {}", username);
            throw new IllegalArgumentException("Invalid username or password.");
        }

        logger.info("Login successful for username: {}", username);
        return "Login successful!";
    }

    @Override
    public void logoutAdmin(String username) {
        logger.info("Processing logout for username: {}", username);
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Admin not found with username: {}", username);
                    return new UserNotFoundException("Admin not found with username: " + username);
                });

        logger.info("Admin with username: {} logged out successfully.", username);
    }

    @Override
    public void deleteAdmin(Long adminId) {
        logger.info("Deleting admin with ID: {}", adminId);
        Admin existingAdmin = adminRepository.findById(adminId)
                .orElseThrow(() -> new UserNotFoundException("Admin with ID " + adminId + " not found"));

        adminRepository.deleteById(adminId);
        logger.info("Successfully deleted admin with ID: {}", adminId);
    }

    @Override
    public Admin getAdminById(Long adminId) {
        logger.info("Fetching admin by ID: {}", adminId);
        return adminRepository.findById(adminId)
                .orElseThrow(() -> new UserNotFoundException("Admin with ID " + adminId + " not found"));
    }

    @Override
    public Admin getAdminByUsername(String username) {
        logger.info("Fetching admin by username: {}", username);
        return adminRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Admin with username " + username + " not found"));
    }

    @Override
    public List<Admin> getAllAdmins() {
        logger.info("Fetching all admins");
        List<Admin> admins = adminRepository.findAll();
        if (admins.isEmpty()) {
            logger.warn("No admins found in the database.");
        }
        return admins;
    }

    @Override
    public String updateAdmin(Long adminId, Admin admin) {
        logger.info("Updating admin with ID: {}", adminId);
        validateUserData(admin);

        Admin existingAdmin = adminRepository.findById(adminId)
                .orElseThrow(() -> new UserNotFoundException("Admin not found with ID: " + adminId));

        existingAdmin.setFullname(admin.getFullname());
        existingAdmin.setUsername(admin.getUsername());
        existingAdmin.setMobileNo(admin.getMobileNo());
        existingAdmin.setEmail(admin.getEmail());

        if (admin.getProfilePicture() != null && admin.getProfilePicture().length > 0) {
            existingAdmin.setProfilePicture(admin.getProfilePicture());
        }

        adminRepository.save(existingAdmin);
        logger.info("Successfully updated admin with ID: {}", adminId);
        return "Admin updated successfully.";
    }


    @Override
    @jakarta.transaction.Transactional
    public PropertyNew updateAgentAndProperty(Long propertyId, PropertyNew updatedProperty, List<MultipartFile> newImages) {
        try {
            logger.info("Updating property with ID: {}", propertyId);

            // Fetch the existing property from the database
            PropertyNew existingProperty = propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new IllegalArgumentException("The Property you are trying to update not found"));

            // Fetch agent only if it's updated
            if (updatedProperty.getAgent() != null && updatedProperty.getAgent().getId() != null) {
                Agent existingAgent = agentRepository.findById(updatedProperty.getAgent().getId())
                        .orElseThrow(() -> new AgentNotFoundException("Agent not found with ID: " + updatedProperty.getAgent().getId()));
                existingProperty.setAgent(existingAgent); // ✅ Assign a managed agent
            }

            // Update other fields
            if (updatedProperty.getTitle() != null) existingProperty.setTitle(updatedProperty.getTitle());
            if (updatedProperty.getPrice() != null) existingProperty.setPrice(updatedProperty.getPrice());
            if (updatedProperty.getSize() != null) existingProperty.setSize(updatedProperty.getSize());
            if (updatedProperty.getAddress() != null) existingProperty.setAddress(updatedProperty.getAddress());
            if (updatedProperty.getYearBuilt() != null) existingProperty.setYearBuilt(updatedProperty.getYearBuilt());
            if (updatedProperty.getPropertyType() != null) existingProperty.setPropertyType(updatedProperty.getPropertyType());
            if (updatedProperty.getBedrooms() != null) existingProperty.setBedrooms(updatedProperty.getBedrooms());
            if (updatedProperty.getBathrooms() != null) existingProperty.setBathrooms(updatedProperty.getBathrooms());
            if (updatedProperty.getAmenities() != null) existingProperty.setAmenities(updatedProperty.getAmenities());
            if (updatedProperty.getFeatures() != null) existingProperty.setFeatures(updatedProperty.getFeatures());
            if (updatedProperty.getProximity() != null) existingProperty.setProximity(updatedProperty.getProximity());

            // If there are new images, update the gallery images list
            if (newImages != null && !newImages.isEmpty()) {
                List<byte[]> updatedImageList = new ArrayList<>();
                for (MultipartFile image : newImages) {
                    updatedImageList.add(image.getBytes());
                }
                existingProperty.setGalleryImages(updatedImageList);
            }

            // Update timestamps
            existingProperty.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

            // Save the updated property
            PropertyNew savedProperty = propertyRepository.save(existingProperty);

            logger.info("Property updated successfully. Property ID: {}", savedProperty.getPropertyId());
            return savedProperty;
        } catch (Exception e) {
            logger.error("Error updating property with ID: {}", propertyId, e);
            throw new RuntimeException("Error updating property", e);
        }
    }

    private void validateUserData(Admin admin) {
        if (admin.getUsername() == null || !ValidationClass.USERNAME_PATTERN.matcher(admin.getUsername()).matches()) {
            throw new IllegalArgumentException("Username is required and should be alphanumeric.");
        }
        if (admin.getEmail() == null || !ValidationClass.EMAIL_PATTERN.matcher(admin.getEmail()).matches()) {
            throw new IllegalArgumentException("Email is not valid.");
        }
        if (admin.getMobileNo() == null || !ValidationClass.PHONE_PATTERN.matcher(admin.getMobileNo()).matches()) {
            throw new IllegalArgumentException("Mobile number should be 10 digits.");
        }
        if (admin.getPassword() == null || !ValidationClass.PASSWORD_PATTERN.matcher(admin.getPassword()).matches()) {
            throw new IllegalArgumentException("Password should be 6-20 characters, including at least one letter, one number, and one special character.");
        }
    }
}
