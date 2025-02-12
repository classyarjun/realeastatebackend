package com.RealEstateDevelopment.Controller;

import com.RealEstateDevelopment.Entity.Agent;
import com.RealEstateDevelopment.Entity.TemporaryAgent;
import com.RealEstateDevelopment.Service.AgentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agents")
@CrossOrigin("*")
public class AgentController {

    @Autowired
    private AgentService agentService;

    private static final Logger logger = LoggerFactory.getLogger(AgentController.class);


    @PostMapping("/registerTemporaryAgent")
    public ResponseEntity<?> registerTemporaryAgent(
            @RequestPart("agent") String agentJson,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {
        try {
            logger.info("Registering a new temporary agent.");
            ObjectMapper objectMapper = new ObjectMapper();
            TemporaryAgent agent = objectMapper.readValue(agentJson, TemporaryAgent.class);

            TemporaryAgent registeredAgent = agentService.registerTemporaryAgent(agent, profilePicture);
            logger.info("Temporary agent registered successfully with ID: {}", registeredAgent.getId());
            return ResponseEntity.ok(registeredAgent);
        } catch (Exception e) {
            logger.error("Error during agent registration: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error during agent registration: " + e.getMessage());
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
    public ResponseEntity<Map<String, Object>> rejectAgent(@PathVariable Long tempAgentId) {
        Map<String, Object> response = new HashMap<>();
        try {
            logger.info("Rejecting agent with temporary ID: {}", tempAgentId);

            agentService.rejectAgent(tempAgentId);

            logger.info("Agent rejected successfully.");

            response.put("status", "success");
            response.put("message", "Agent rejected successfully.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error rejecting agent: {}", e.getMessage(), e);

            response.put("status", "error");
            response.put("message", "Error rejecting agent: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
    /**
     * Get all pending agent approvals.
     */
    @GetMapping("/getAllPendingAgents")
    public ResponseEntity<List<TemporaryAgent>> getPendingAgents() {
        logger.info("Fetching all pending agents.");
        return ResponseEntity.ok(agentService.getAllPendingAgents());
    }

    @PostMapping(value = "/loginAgent", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> loginAgent(@RequestParam("username") String username,
                                        @RequestParam("password") String password) {
        try {
            logger.info("Attempting to login agent with username: {}", username);

            Agent agent = agentService.loginAgent(username, password);
            logger.info("Agent {} logged in successfully.", username);
            return ResponseEntity.ok(agent);
        } catch (Exception e) {
            logger.error("Error during agent login: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error during agent login: " + e.getMessage());
        }
    }

    @PutMapping("/updateAgent/{agentId}")
    public ResponseEntity<?> updateAgent(
            @PathVariable Long agentId,
            @RequestPart("agent") String agentJson,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {
        try {
            logger.info("Attempting to update agent with ID: {}", agentId);
            ObjectMapper objectMapper = new ObjectMapper();
            Agent updatedAgent = objectMapper.readValue(agentJson, Agent.class);

            Agent agent = agentService.updateAgent(agentId, updatedAgent, profilePicture);
            logger.info("Agent with ID: {} updated successfully.", agentId);
            return ResponseEntity.ok(agent);
        } catch (Exception e) {
            logger.error("Error updating agent with ID: {}: {}", agentId, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error updating agent: " + e.getMessage());
        }
    }

    @DeleteMapping("/deleteAgent/{agentId}")
    public ResponseEntity<?> deleteAgent(@PathVariable Long agentId) {
        try {
            logger.info("Attempting to delete agent with ID: {}", agentId);
            agentService.deleteAgent(agentId);
            logger.info("Agent with ID: {} deleted successfully.", agentId);
            return ResponseEntity.ok("Agent deleted successfully.");
        } catch (Exception e) {
            logger.error("Error deleting agent with ID: {}: {}", agentId, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error deleting agent: " + e.getMessage());
        }
    }

    @GetMapping("/getAgentById/{agentId}")
    public ResponseEntity<?> getAgentById(@PathVariable Long agentId) {
        try {
            logger.info("Fetching agent details with ID: {}", agentId);
            Agent agent = agentService.getAgentById(agentId);
            logger.info("Agent with ID: {} retrieved successfully.", agentId);
            return ResponseEntity.ok(agent);
        } catch (Exception e) {
            logger.error("Error fetching agent with ID: {}: {}", agentId, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error fetching agent: " + e.getMessage());
        }
    }

    @GetMapping("/getAllAgents")
    public ResponseEntity<?> getAllAgents() {
        try {
            logger.info("Fetching all agents.");
            List<Agent> agents = agentService.getAllAgents();
            logger.info("Successfully retrieved {} agents.", agents.size());
            return ResponseEntity.ok(agents);
        } catch (Exception e) {
            logger.error("Error fetching all agents: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error fetching all agents: " + e.getMessage());
        }
    }

    @PutMapping("/changeAgentPassword/{agentId}")
    public ResponseEntity<?> changeAgentPassword(
            @PathVariable Long agentId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        try {
            logger.info("Attempting to change password for agent with ID: {}", agentId);
            agentService.changeAgentPassword(agentId, oldPassword, newPassword);
            logger.info("Password changed successfully for agent with ID: {}", agentId);
            return ResponseEntity.ok("Password changed successfully.");
        } catch (Exception e) {
            logger.error("Error changing password for agent with ID: {}: {}", agentId, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error changing password: " + e.getMessage());
        }
    }

    // Here are your updated methods using ResponseEntity<Map<String, String>>:
//    @PostMapping("/registerTemporaryAgent")
//    public ResponseEntity<Map<String, String>> registerTemporaryAgent(
//            @RequestPart("agent") String agentJson,
//            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {
//
//        Map<String, String> response = new HashMap<>();
//
//        try {
//            logger.info("Registering a new temporary agent.");
//            ObjectMapper objectMapper = new ObjectMapper();
//            TemporaryAgent agent = objectMapper.readValue(agentJson, TemporaryAgent.class);
//
//            TemporaryAgent registeredAgent = agentService.registerTemporaryAgent(agent, profilePicture);
//            logger.info("Temporary agent registered successfully with ID: {}", registeredAgent.getId());
//
//            response.put("message", "Temporary agent registered successfully.");
//            response.put("agentId", String.valueOf(registeredAgent.getId()));
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            logger.error("Error during agent registration: {}", e.getMessage(), e);
//            response.put("message", "Error during agent registration: " + e.getMessage());
//            return ResponseEntity.badRequest().body(response);
//        }
//    }
//
//    /**
//     * Approve an agent by admin.
//     */
//    @PostMapping("/approveAgent/{tempAgentId}")
//    public ResponseEntity<Map<String, String>> approveAgent(@PathVariable Long tempAgentId) {
//        Map<String, String> response = new HashMap<>();
//
//        try {
//            logger.info("Approving agent with temporary ID: {}", tempAgentId);
//            Agent approvedAgent = agentService.approveAgent(tempAgentId);
//            logger.info("Agent approved successfully with ID: {}", approvedAgent.getId());
//
//            response.put("message", "Agent approved successfully.");
//            response.put("agentId", String.valueOf(approvedAgent.getId()));
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            logger.error("Error approving agent: {}", e.getMessage(), e);
//            response.put("message", "Error approving agent: " + e.getMessage());
//            return ResponseEntity.badRequest().body(response);
//        }
//    }
//
//    /**
//     * Reject an agent by admin.
//     */
//    @DeleteMapping("/rejectAgent/{tempAgentId}")
//    public ResponseEntity<Map<String, String>> rejectAgent(@PathVariable Long tempAgentId) {
//        Map<String, String> response = new HashMap<>();
//
//        try {
//            logger.info("Rejecting agent with temporary ID: {}", tempAgentId);
//            agentService.rejectAgent(tempAgentId);
//            logger.info("Agent rejected successfully.");
//
//            response.put("message", "Agent rejected successfully.");
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            logger.error("Error rejecting agent: {}", e.getMessage(), e);
//            response.put("message", "Error rejecting agent: " + e.getMessage());
//            return ResponseEntity.badRequest().body(response);
//        }
//    }
//
//    @PostMapping("/loginAgent")
//    public ResponseEntity<Map<String, String>> loginAgent(@RequestBody Map<String, String> loginDetails) {
//        Map<String, String> response = new HashMap<>();
//
//        try {
//            logger.info("Attempting to login agent with username: {}", loginDetails.get("username"));
//            String username = loginDetails.get("username");
//            String password = loginDetails.get("password");
//
//            Agent agent = agentService.loginAgent(username, password);
//            logger.info("Agent {} logged in successfully.", username);
//
//            response.put("message", "Login successful.");
//            response.put("agentId", String.valueOf(agent.getId()));
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            logger.error("Error during agent login: {}", e.getMessage(), e);
//            response.put("message", "Error during agent login: " + e.getMessage());
//            return ResponseEntity.badRequest().body(response);
//        }
//    }
//
//    @DeleteMapping("/deleteAgent/{agentId}")
//    public ResponseEntity<Map<String, String>> deleteAgent(@PathVariable Long agentId) {
//        Map<String, String> response = new HashMap<>();
//
//        try {
//            logger.info("Attempting to delete agent with ID: {}", agentId);
//            agentService.deleteAgent(agentId);
//            logger.info("Agent with ID: {} deleted successfully.", agentId);
//
//            response.put("message", "Agent deleted successfully.");
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            logger.error("Error deleting agent with ID: {}: {}", agentId, e.getMessage(), e);
//            response.put("message", "Error deleting agent: " + e.getMessage());
//            return ResponseEntity.badRequest().body(response);
//        }
//    }

}
