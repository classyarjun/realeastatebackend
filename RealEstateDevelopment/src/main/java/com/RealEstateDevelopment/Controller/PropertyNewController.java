package com.RealEstateDevelopment.Controller;

import com.RealEstateDevelopment.Entity.PendingProperty;
import com.RealEstateDevelopment.Entity.PropertyNew;
import com.RealEstateDevelopment.Exceptions.PropertyNotFoundException;
import com.RealEstateDevelopment.Service.PropertyNewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/properties")
public class PropertyNewController {
    private static final Logger logger = LoggerFactory.getLogger(PropertyNewController.class);

    @Autowired
    private PropertyNewService propertyNewService;


    // Add Property

    @PostMapping("/addProperty/{agentId}")
    public ResponseEntity<Map<String, Object>> addProperty(
            @PathVariable Long agentId,
            @RequestPart("property") String propertyJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        Map<String, Object> response = new HashMap<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            PendingProperty pendingProperty = objectMapper.readValue(propertyJson, PendingProperty.class);
            PendingProperty savedProperty = propertyNewService.addProperty(agentId, pendingProperty, images);

            response.put("status", "success");
            response.put("message", "Property added successfully.");
            response.put("property", savedProperty);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error adding property: {}", e.getMessage(), e);

            response.put("status", "error");
            response.put("message", "Error adding property: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    // Approve Property
//    @CrossOrigin(origins = "http://localhost:4200")
    @PutMapping("/approveProperty/{propertyId}")
    public ResponseEntity<Map<String, Object>> approveProperty(@PathVariable Long propertyId) {
        Map<String, Object> response = new HashMap<>();

        try {
            propertyNewService.approveProperty(propertyId);

            response.put("status", "success");
            response.put("message", "Property approved successfully");
            response.put("propertyId", propertyId);

            return ResponseEntity.ok(response);
        } catch (PropertyNotFoundException e) {
            logger.error("Property not found with ID: {}", propertyId);

            response.put("status", "error");
            response.put("message", e.getMessage());
            response.put("propertyId", propertyId);

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            logger.error("Error approving property with ID {}: {}", propertyId, e.getMessage());

            response.put("status", "error");
            response.put("message", "Error approving property: " + e.getMessage());
            response.put("propertyId", propertyId);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Reject Property
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


//    // Approve Property
//    @PutMapping("/approveProperty/{propertyId}")
//    public ResponseEntity<?> approveProperty(@PathVariable Long propertyId) {
//        try {
//            PropertyNew approvedProperty = propertyNewService.approveProperty(propertyId);
//            return ResponseEntity.ok(approvedProperty);
//        } catch (Exception e) {
//            logger.error("Error approving property: {}", e.getMessage(), e);
//            return ResponseEntity.badRequest().body("Error approving property: " + e.getMessage());
//        }
//    }
//
//    @DeleteMapping("/rejectProperty/{propertyId}")
//    public ResponseEntity<?> rejectProperty(@PathVariable Long propertyId) {
//        try {
//            logger.info("Rejecting and deleting property with ID: {}", propertyId);
//            propertyNewService.rejectProperty(propertyId);
//            logger.info("Property with ID {} rejected and deleted successfully", propertyId);
//            return ResponseEntity.ok("Property rejected and deleted successfully");
//        } catch (PropertyNotFoundException e) {
//            logger.error("Property not found with ID: {}", propertyId);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
//        } catch (Exception e) {
//            logger.error("Error rejecting and deleting property with ID {}: {}", propertyId, e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Error rejecting and deleting property: " + e.getMessage());
//        }
//    }


    // Get Property by ID
    @GetMapping("/getPropertyByIds/{propertyId}")
    public ResponseEntity<?> getPropertyById(@PathVariable Long propertyId) {
        try {
            logger.info("Fetching property with ID: {}", propertyId);
            PropertyNew property = propertyNewService.getPropertyById(propertyId);
            if (property == null) {
                logger.warn("Property with ID {} not found", propertyId);
                return ResponseEntity.notFound().build();  // Return 404 Not Found if property not found
            }
            logger.info("Property with ID {} fetched successfully", propertyId);
            return ResponseEntity.ok(property);
        } catch (Exception e) {
            logger.error("Error fetching property with ID {}: {}", propertyId, e.getMessage());
            return ResponseEntity.status(500).body("Error fetching property: " + e.getMessage());
        }
    }

    // Get All Properties
    @GetMapping("/getAllTheProperties")
    public ResponseEntity<List<PropertyNew>> getAllProperties() {
        try {
            logger.info("Fetching all properties");
            List<PropertyNew> properties = propertyNewService.getAllProperties();
            if (properties.isEmpty()) {
                logger.warn("No properties found");
                return ResponseEntity.noContent().build();  // Return 204 No Content if no properties exist
            }
            logger.info("Fetched {} properties", properties.size());
            return ResponseEntity.ok(properties);
        } catch (Exception e) {
            logger.error("Error fetching all properties: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
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

    // Get Pending Properties by Agent ID
    @GetMapping("/getPendingPropertiesByAgent/{agentId}")
    public ResponseEntity<?> getPendingPropertiesByAgent(@PathVariable Long agentId) {
        try {
            logger.info("Fetching pending properties for agent with ID: {}", agentId);
            List<PendingProperty> pendingProperties = propertyNewService.getPendingPropertiesByAgent(agentId);
            if (pendingProperties.isEmpty()) {
                logger.warn("No pending properties found for agent ID {}", agentId);
                return ResponseEntity.noContent().build();  // Return 204 No Content if no pending properties found
            }
            logger.info("Fetched {} pending properties for agent ID {}", pendingProperties.size(), agentId);
            return ResponseEntity.ok(pendingProperties);
        } catch (Exception e) {
            logger.error("Error fetching pending properties for agent ID {}: {}", agentId, e.getMessage());
            return ResponseEntity.status(500).body("Error fetching pending properties by agent: " + e.getMessage());
        }
    }

    // Get Properties by Agent ID
    @GetMapping("/getPropertiesByAgentId/{agentId}")
    public ResponseEntity<?> getPropertiesByAgentId(@PathVariable Long agentId) {
        try {
            logger.info("Fetching properties for agent with ID: {}", agentId);
            List<PropertyNew> properties = propertyNewService.getPropertiesByAgentId(agentId);
            if (properties.isEmpty()) {
                logger.warn("No properties found for agent ID {}", agentId);
                return ResponseEntity.noContent().build();  // Return 204 No Content if no properties found
            }
            logger.info("Fetched {} properties for agent ID {}", properties.size(), agentId);
            return ResponseEntity.ok(properties);
        } catch (Exception e) {
            logger.error("Error fetching properties for agent ID {}: {}", agentId, e.getMessage());
            return ResponseEntity.status(500).body("Error fetching properties by agent: " + e.getMessage());
        }
    }

    // Search Properties by Keyword
    @GetMapping("/searchProperties")
    public ResponseEntity<?> searchProperties(@RequestParam String keyword) {
        try {
            logger.info("Searching for properties with keyword: {}", keyword);
            List<PropertyNew> properties = propertyNewService.searchProperties(keyword);
            if (properties.isEmpty()) {
                logger.warn("No properties found for keyword {}", keyword);
                return ResponseEntity.noContent().build();  // Return 204 No Content if no properties match the keyword
            }
            logger.info("Fetched {} properties for keyword {}", properties.size(), keyword);
            return ResponseEntity.ok(properties);
        } catch (Exception e) {
            logger.error("Error searching properties with keyword {}: {}", keyword, e.getMessage());
            return ResponseEntity.status(500).body("Error searching properties: " + e.getMessage());
        }
    }

    // enhanced searchProperties method and provide more search filters

    @GetMapping("/searchProperty")
    public ResponseEntity<List<PropertyNew>> searchProperty(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String propertyType,
            @RequestParam(required = false) Integer minBedrooms,
            @RequestParam(required = false) Integer maxBedrooms,
            @RequestParam(required = false) Integer minBathrooms,
            @RequestParam(required = false) Integer maxBathrooms,
            @RequestParam(required = false) String amenities,
            @RequestParam(required = false) String features) {

        try {
            List<PropertyNew> properties = propertyNewService.searchProperty(
                    keyword, minPrice, maxPrice, propertyType, minBedrooms, maxBedrooms,
                    minBathrooms, maxBathrooms, amenities, features
            );
            return ResponseEntity.ok(properties);
        } catch (Exception e) {
            logger.error("Error searching properties", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    // Update Property
    @PutMapping("/updateProperty/{propertyId}")
    public ResponseEntity<?> updateProperty(@PathVariable Long propertyId,
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

    // Delete Property by ID
    @DeleteMapping("/deleteProperty/{propertyId}")
    public ResponseEntity<Map<String, Object>> deleteProperty(@PathVariable Long propertyId) {
        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("Deleting property with ID: {}", propertyId);
            propertyNewService.deleteProperty(propertyId);
            logger.info("Property with ID {} deleted successfully", propertyId);

            response.put("status", "success");
            response.put("message", "Property deleted successfully");
            response.put("propertyId", propertyId);

            return ResponseEntity.ok(response);
        } catch (PropertyNotFoundException e) {
            logger.error("Property not found with ID: {}", propertyId);

            response.put("status", "error");
            response.put("message", "Property not found");
            response.put("propertyId", propertyId);

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            logger.error("Error deleting property with ID {}: {}", propertyId, e.getMessage());

            response.put("status", "error");
            response.put("message", "Error deleting property: " + e.getMessage());
            response.put("propertyId", propertyId);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    // Endpoint to update images for a property
    // Add images to Property
    @PostMapping("/addImagesToProperty/{propertyId}")
    public ResponseEntity<String> addImagesToProperty(@PathVariable Long propertyId,
                                                      @RequestParam("images") List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            logger.warn("No images provided for property with ID: {}", propertyId);
            return ResponseEntity.badRequest().body("No images provided");
        }
        try {
            logger.info("Adding images to property with ID: {}", propertyId);
            propertyNewService.addImagesToProperty(propertyId, images);
            logger.info("Images added successfully to property with ID: {}", propertyId);
            return ResponseEntity.ok("Images added successfully to the property");
        } catch (Exception e) {
            logger.error("Error adding images to property with ID {}: {}", propertyId, e.getMessage());
            return ResponseEntity.status(500).body("Error adding images: " + e.getMessage());
        }
    }

    // Update Property Images
    @PutMapping("/updatePropertyImages/{propertyId}")
    public ResponseEntity<String> updatePropertyImages(@PathVariable Long propertyId,
                                                       @RequestParam("images") List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            logger.warn("No images provided for updating property with ID: {}", propertyId);
            return ResponseEntity.badRequest().body("No images provided for update");
        }
        try {
            logger.info("Updating images for property with ID: {}", propertyId);
            propertyNewService.updateImages(propertyId, images);
            logger.info("Images updated successfully for property with ID: {}", propertyId);
            return ResponseEntity.ok("Images updated successfully");
        } catch (Exception e) {
            logger.error("Error updating images for property with ID {}: {}", propertyId, e.getMessage());
            return ResponseEntity.status(500).body("Error updating images: " + e.getMessage());
        }
    }

    // Get all images for Property
    @GetMapping("/getPropertyImages/{propertyId}")
    public ResponseEntity<List<byte[]>> getPropertyImages(@PathVariable Long propertyId) {
        try {
            logger.info("Fetching images for property with ID: {}", propertyId);
            List<byte[]> images = propertyNewService.getImages(propertyId);
            if (images == null || images.isEmpty()) {
                logger.warn("No images found for property with ID: {}", propertyId);
                return ResponseEntity.noContent().build();  // Return 204 No Content if no images are found
            }
            logger.info("Fetched {} images for property with ID: {}", images.size(), propertyId);
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            logger.error("Error fetching images for property with ID {}: {}", propertyId, e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }
}
