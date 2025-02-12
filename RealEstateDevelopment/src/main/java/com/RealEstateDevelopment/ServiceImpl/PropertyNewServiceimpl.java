package com.RealEstateDevelopment.ServiceImpl;

import com.RealEstateDevelopment.Entity.*;
import com.RealEstateDevelopment.Exception.AgentNotFoundException;
import com.RealEstateDevelopment.Exceptions.PropertyNotFoundException;
import com.RealEstateDevelopment.Repository.AgentRepository;
import com.RealEstateDevelopment.Repository.PendingPropertyRepository;
import com.RealEstateDevelopment.Repository.PropertyNewRepository;
import com.RealEstateDevelopment.Service.EmailService;
import com.RealEstateDevelopment.Service.PropertyNewService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
public class PropertyNewServiceimpl implements PropertyNewService {

    private static final Logger logger = LoggerFactory.getLogger(PropertyNewServiceimpl.class);

    @Autowired
    private PendingPropertyRepository pendingPropertyRepository;

    @Autowired
    private PropertyNewRepository propertyRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private EmailService emailService;


    @Override
    @Transactional
    public PendingProperty addProperty(Long agentId, PendingProperty pendingProperty, List<MultipartFile> images) {
        try {
            logger.info("Agent adding a new property for approval, Agent ID: {}", agentId);

            Agent agent = agentRepository.findById(agentId)
                    .orElseThrow(() -> new IllegalArgumentException("Agent not found"));

            pendingProperty.setAgent(agent);
            pendingProperty.setStatus(Status.PENDING);
            pendingProperty.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            pendingProperty.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

            // Store images
            if (images != null && !images.isEmpty()) {
                List<byte[]> imageList = new ArrayList<>();
                for (MultipartFile image : images) {
                    try {
                        imageList.add(image.getBytes());
                    } catch (IOException e) {
                        logger.error("Error processing image", e);
                        throw new RuntimeException("Error processing image", e);
                    }
                }
                pendingProperty.setImages(imageList);
            }

            PendingProperty savedProperty = pendingPropertyRepository.save(pendingProperty);

            // Send email notification to admin
            String adminEmail = "bhagwatpatil1110@gmail.com";
            emailService.sendEmail(agent.getEmail(), adminEmail, "New Property Pending Approval",
                    "Dear Admin,\n\nA new property titled '" + pendingProperty.getTitle() + "' is awaiting your approval.\n\nBest regards,\nRealEstate Team");

            logger.info("Property added and awaiting approval. Property ID: {}", savedProperty.getPropertyId());
            return savedProperty;
        } catch (Exception e) {
            logger.error("Error adding property for Agent ID: {}", agentId, e);
            throw new RuntimeException("Error adding property", e);
        }
    }


    @Override
    @Transactional
    public PropertyNew approveProperty(Long propertyId) throws Exception {
        PendingProperty pendingProperty = pendingPropertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Pending property not found"));

        PropertyNew property = new PropertyNew();
        property.setTitle(pendingProperty.getTitle());
        property.setPrice(pendingProperty.getPrice());
        property.setSize(pendingProperty.getSize());
        property.setAddress(pendingProperty.getAddress());
        property.setYearBuilt(pendingProperty.getYearBuilt());
        property.setPropertyType(pendingProperty.getPropertyType());
        property.setBedrooms(pendingProperty.getBedrooms());
        property.setBathrooms(pendingProperty.getBathrooms());
        property.setAmenities(pendingProperty.getAmenities());
        property.setFeatures(pendingProperty.getFeatures());
        property.setProximity(pendingProperty.getProximity());
        property.setStatus(Status.APPROVED);
        property.setAvailability(Status.AVAILABLE);
        property.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        property.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        property.setAgent(pendingProperty.getAgent());

        // Move images from PendingProperty to PropertyNew
        if (pendingProperty.getImages() != null && !pendingProperty.getImages().isEmpty()) {
            // Assuming pendingProperty.getImages() returns List<byte[]>, which contains byte arrays for the images
            property.setGalleryImages(new ArrayList<>(pendingProperty.getImages())); // Copy images directly
        }

        PropertyNew savedProperty = propertyRepository.save(property);

        // Send email notification to the agent
        String adminEmail = "bhagwatpatil1110@gmail.com";
        emailService.sendEmail(adminEmail, pendingProperty.getAgent().getEmail(), "Property Approved",
                "Dear " + pendingProperty.getAgent().getFullName() + ",\n\nYour property titled '" + pendingProperty.getTitle() + "' has been approved.\n\nBest regards,\nRealEstate Team");

        // Remove the pending property after approval
        pendingPropertyRepository.delete(pendingProperty);

        logger.info("Property approved and status updated. Property ID: {}", savedProperty.getPropertyId());
        return savedProperty;
    }



    // ******************** added methods for properties ********************

    @Override
    public PropertyNew getPropertyById(Long propertyId) {
        try {
            return propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new PropertyNotFoundException("Property not found with ID: " + propertyId));
        } catch (PropertyNotFoundException e) {
            logger.error("Error fetching property with ID: {}", propertyId, e);
            throw e;  // rethrowing exception
        }
    }

    @Override
    public List<PropertyNew> getAllProperties() {
        try {
            return propertyRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching all properties", e);
            throw new RuntimeException("Error fetching all properties", e);
        }
    }

    @Override
    public List<PendingProperty> getAllPendingProperties() {
        try {
            return pendingPropertyRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching all pending properties", e);
            throw new RuntimeException("Error fetching all pending properties", e);
        }
    }

    @Override
    public List<PendingProperty> getPendingPropertiesByAgent(Long agentId) {
        try {
            return pendingPropertyRepository.findByAgent_Id(agentId);
        } catch (Exception e) {
            logger.error("Error fetching pending properties for agent with ID: {}", agentId, e);
            throw new RuntimeException("Error fetching pending properties", e);
        }
    }

    @Override
    public List<PropertyNew> getPropertiesByAgentId(Long agentId) {
        try {
            return propertyRepository.findByAgent_Id(agentId);
        } catch (Exception e) {
            logger.error("Error fetching properties for agent with ID: {}", agentId, e);
            throw new RuntimeException("Error fetching properties by agent", e);
        }
    }

    @Override
    public List<PropertyNew> searchProperties(String keyword) {
        try {
            return propertyRepository.findByTitleContainingIgnoreCaseOrAddressContainingIgnoreCase(keyword, keyword);
        } catch (Exception e) {
            logger.error("Error searching properties with keyword: {}", keyword, e);
            throw new RuntimeException("Error searching properties", e);
        }
    }

    // enhanced searchProperties method and provide more search filters

    @Override
    public List<PropertyNew> searchProperty(String keyword, Double minPrice, Double maxPrice,
                                            String propertyType, Integer minBedrooms, Integer maxBedrooms,
                                            Integer minBathrooms, Integer maxBathrooms,
                                            String amenities, String features) {
        try {
            // Build a specification or a query dynamically based on the provided filters
            Specification<PropertyNew> specification = Specification.where(null);

            if (keyword != null && !keyword.isEmpty()) {
                specification = specification.and((root, query, builder) -> builder.or(
                        builder.like(builder.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"),
                        builder.like(builder.lower(root.get("address")), "%" + keyword.toLowerCase() + "%")
                ));
            }

            if (minPrice != null) {
                specification = specification.and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                specification = specification.and((root, query, builder) -> builder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (propertyType != null && !propertyType.isEmpty()) {
                specification = specification.and((root, query, builder) -> builder.equal(root.get("propertyType"), propertyType));
            }

            if (minBedrooms != null) {
                specification = specification.and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("bedrooms"), minBedrooms));
            }

            if (maxBedrooms != null) {
                specification = specification.and((root, query, builder) -> builder.lessThanOrEqualTo(root.get("bedrooms"), maxBedrooms));
            }

            if (minBathrooms != null) {
                specification = specification.and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("bathrooms"), minBathrooms));
            }

            if (maxBathrooms != null) {
                specification = specification.and((root, query, builder) -> builder.lessThanOrEqualTo(root.get("bathrooms"), maxBathrooms));
            }

            if (amenities != null && !amenities.isEmpty()) {
                specification = specification.and((root, query, builder) -> builder.like(builder.lower(root.get("amenities")), "%" + amenities.toLowerCase() + "%"));
            }

            if (features != null && !features.isEmpty()) {
                specification = specification.and((root, query, builder) -> builder.like(builder.lower(root.get("features")), "%" + features.toLowerCase() + "%"));
            }

            // Execute the query with the dynamic specification
            return propertyRepository.findAll(specification);

        } catch (Exception e) {
            logger.error("Error searching properties with filters", e);
            throw new RuntimeException("Error searching properties", e);
        }
    }


    @Override
    public List<PendingProperty> getPendingPropertiesByAgentId(Long agentId) {
        try {
            return pendingPropertyRepository.findByAgent_Id(agentId);
        } catch (Exception e) {
            logger.error("Error fetching pending properties by agent ID: {}", agentId, e);
            throw new RuntimeException("Error fetching pending properties by agent", e);
        }
    }


//    @Override
//    @Transactional
//    public PropertyNew updateProperty(Long propertyId, PropertyNew updatedProperty, List<MultipartFile> newImages) {
//        try {
//            logger.info("Updating property with ID: {}", propertyId);
//
//            // Fetch the existing property from the database
//            PropertyNew existingProperty = propertyRepository.findById(propertyId)
//                    .orElseThrow(() -> new IllegalArgumentException("The Property you are trying to update not found"));
//
//            // Update the fields if new values are provided
//            if (updatedProperty.getTitle() != null) {
//                existingProperty.setTitle(updatedProperty.getTitle());
//            }
//            if (updatedProperty.getPrice() != null) {
//                existingProperty.setPrice(updatedProperty.getPrice());
//            }
//            if (updatedProperty.getSize() != null) {
//                existingProperty.setSize(updatedProperty.getSize());
//            }
//            if (updatedProperty.getAddress() != null) {
//                existingProperty.setAddress(updatedProperty.getAddress());
//            }
//            if (updatedProperty.getYearBuilt() != null) {
//                existingProperty.setYearBuilt(updatedProperty.getYearBuilt());
//            }
//            if (updatedProperty.getPropertyType() != null) {
//                existingProperty.setPropertyType(updatedProperty.getPropertyType());
//            }
//            if (updatedProperty.getBedrooms() != null) {
//                existingProperty.setBedrooms(updatedProperty.getBedrooms());
//            }
//            if (updatedProperty.getBathrooms() != null) {
//                existingProperty.setBathrooms(updatedProperty.getBathrooms());
//            }
//            if (updatedProperty.getAmenities() != null) {
//                existingProperty.setAmenities(updatedProperty.getAmenities());
//            }
//            if (updatedProperty.getFeatures() != null) {
//                existingProperty.setFeatures(updatedProperty.getFeatures());
//            }
//            if (updatedProperty.getProximity() != null) {
//                existingProperty.setProximity(updatedProperty.getProximity());
//            }
//
//            // If there are new images, update the gallery images list
//            if (newImages != null && !newImages.isEmpty()) {
//                List<byte[]> updatedImageList = new ArrayList<>();
//                for (MultipartFile image : newImages) {
//                    try {
//                        updatedImageList.add(image.getBytes());
//                    } catch (IOException e) {
//                        logger.error("Error processing image", e);
//                        throw new RuntimeException("Error processing image", e);
//                    }
//                }
//                existingProperty.setGalleryImages(updatedImageList);
//            }
//
//            // Update timestamps
//            existingProperty.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
//
//            // Save the updated property back into the repository
//            PropertyNew savedProperty = propertyRepository.save(existingProperty);
//
//            logger.info("Property updated successfully. Property ID: {}", savedProperty.getPropertyId());
//            return savedProperty;
//        } catch (Exception e) {
//            logger.error("Error updating property with ID: {}", propertyId, e);
//            throw new RuntimeException("Error updating property", e);
//        }
//    }

    @Override
    @Transactional
    public PropertyNew updateProperty(Long propertyId, PropertyNew updatedProperty, List<MultipartFile> newImages) {
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



    @Override
    @Transactional
    public void deleteProperty(Long propertyId) {
        try {
            PropertyNew property = getPropertyById(propertyId);
            propertyRepository.delete(property);
            logger.info("Property with ID: {} deleted successfully", propertyId);
        } catch (Exception e) {
            logger.error("Error deleting property with ID: {}", propertyId, e);
            throw new RuntimeException("Error deleting property", e);
        }
    }

    @Override
    @Transactional
    public void rejectProperty(Long propertyId) {
        try {
            PendingProperty pendingProperty = pendingPropertyRepository.findById(propertyId)
                    .orElseThrow(() -> new PropertyNotFoundException("Pending property not found with ID: " + propertyId));

            pendingPropertyRepository.delete(pendingProperty);
            logger.info("Property with ID {} rejected and deleted successfully", propertyId);
        } catch (Exception e) {
            logger.error("Error rejecting and deleting property with ID {}: {}", propertyId, e.getMessage());
            throw new RuntimeException("Error rejecting and deleting property", e);
        }
    }

    // property image methods implementation


    @Override
    @Transactional
    public void addImagesToProperty(Long propertyId, List<MultipartFile> images) {
        try {
            PropertyNew existingProperty = propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new IllegalArgumentException("Property not found"));

            List<byte[]> imageList = new ArrayList<>(existingProperty.getGalleryImages()); // Get existing images

            for (MultipartFile image : images) {
                try {
                    imageList.add(image.getBytes());
                } catch (IOException e) {
                    logger.error("Error processing image for property with ID: {}", propertyId, e);
                    throw new RuntimeException("Error processing image", e);
                }
            }

            existingProperty.setGalleryImages(imageList);  // Corrected field name
            propertyRepository.save(existingProperty);

            logger.info("Images added successfully to property with ID: {}", propertyId);
        } catch (Exception e) {
            logger.error("Error adding images to property with ID: {}", propertyId, e);
            throw new RuntimeException("Error adding images", e);
        }
    }


    @Override
    @Transactional
    public void updateImages(Long propertyId, List<MultipartFile> images) {
        try {
            PropertyNew existingProperty = propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new IllegalArgumentException("Property not found"));

            List<byte[]> updatedImages = new ArrayList<>();
            for (MultipartFile image : images) {
                try {
                    updatedImages.add(image.getBytes());
                } catch (IOException e) {
                    logger.error("Error processing image for property with ID: {}", propertyId, e);
                    throw new RuntimeException("Error processing image", e);
                }
            }

            existingProperty.setGalleryImages(updatedImages);  // Corrected field name
            propertyRepository.save(existingProperty);

            logger.info("Images updated successfully for property with ID: {}", propertyId);
        } catch (Exception e) {
            logger.error("Error updating images for property with ID: {}", propertyId, e);
            throw new RuntimeException("Error updating images", e);
        }
    }

    @Override
    public List<byte[]> getImages(Long propertyId) {
        try {
            PropertyNew existingProperty = propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new IllegalArgumentException("Property not found"));

            return existingProperty.getGalleryImages();  // Corrected field name
        } catch (Exception e) {
            logger.error("Error fetching images for property with ID: {}", propertyId, e);
            throw new RuntimeException("Error fetching images", e);
        }
    }

}
