package com.RealEstateDevelopment.ServiceImpl;

import com.RealEstateDevelopment.Entity.Agent;
import com.RealEstateDevelopment.Entity.Role;
import com.RealEstateDevelopment.Entity.Status;
import com.RealEstateDevelopment.Entity.TemporaryAgent;
import com.RealEstateDevelopment.Repository.AgentRepository;
import com.RealEstateDevelopment.Repository.TemporaryAgentRepository;
import com.RealEstateDevelopment.Service.AgentService;
import com.RealEstateDevelopment.CommanUtil.ValidationClass;
import com.RealEstateDevelopment.Service.EmailService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class AgentServiceImpl implements AgentService {

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private TemporaryAgentRepository temporaryAgentRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(AgentServiceImpl.class);

    @Override
    public TemporaryAgent registerTemporaryAgent(TemporaryAgent agent, MultipartFile profilePicture) throws Exception {
        logger.info("Registering a new temporary agent: {}", agent.getEmail());

        // Check if email or username already exists in TemporaryAgent or Agent tables
        if (temporaryAgentRepository.existsByEmail(agent.getEmail()) || agentRepository.existsByEmail(agent.getEmail())) {
            throw new IllegalArgumentException("An agent with this email already exists.");
        }
        if (temporaryAgentRepository.existsByUserName(agent.getUserName()) || agentRepository.existsByUserName(agent.getUserName())) {
            throw new IllegalArgumentException("An agent with this username already exists.");
        }

        agent.setPassword(passwordEncoder.encode(agent.getPassword()));
        agent.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        agent.setApproved(false);
        agent.setStatus(Status.INACTIVE);

        if (profilePicture != null) {
            agent.setProfilePicture(profilePicture.getBytes());
        }

        TemporaryAgent savedAgent = temporaryAgentRepository.save(agent);

        emailService.sendEmail("bhagwatpatil1110@gmail.com", "New Agent Registration",
                "A new agent " + agent.getFullName() + " has registered and is waiting for approval. \n\nBest regards,\nRealEstate Team");

        return savedAgent;
    }


    @Override
    @Transactional
    public Agent approveAgent(Long tempAgentId) throws Exception {
        TemporaryAgent tempAgent = temporaryAgentRepository.findById(tempAgentId)
                .orElseThrow(() -> new IllegalArgumentException("Temporary agent not found"));

        // Check if an agent with the same email or username already exists
        if (agentRepository.existsByEmail(tempAgent.getEmail())) {
            throw new IllegalArgumentException("An agent with this email is already approved.");
        }
        if (agentRepository.existsByUserName(tempAgent.getUserName())) {
            throw new IllegalArgumentException("An agent with this username is already approved.");
        }

        Agent agent = new Agent();
        agent.setUserName(tempAgent.getUserName());
        agent.setFullName(tempAgent.getFullName());
        agent.setEmail(tempAgent.getEmail());
        agent.setPassword(tempAgent.getPassword());
        agent.setMobileNo(tempAgent.getMobileNo());
        agent.setProfilePicture(tempAgent.getProfilePicture());
        agent.setExperience(tempAgent.getExperience());
        agent.setBio(tempAgent.getBio());
        agent.setCreatedAt(tempAgent.getCreatedAt());
        agent.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        agent.setStatus(Status.ACTIVE);
        agent.setApproved(true);
        agent.setRole(Role.AGENT);

        Agent savedAgent = agentRepository.save(agent);

        try {
            emailService.sendEmail(tempAgent.getEmail(), "Approval Notification",
                    "Dear " + tempAgent.getFullName() + ",\n\nYour agent profile has been approved.\n\nBest regards,\nRealEstate Team");

            temporaryAgentRepository.delete(tempAgent); // Delete only if email is successfully sent
        } catch (Exception e) {
            throw new RuntimeException("Agent approved, but failed to send approval email.", e);
        }

        return savedAgent;
    }


    @Override
    public void rejectAgent(Long tempAgentId) throws Exception {
        TemporaryAgent tempAgent = temporaryAgentRepository.findById(tempAgentId)
                .orElseThrow(() -> new IllegalArgumentException("Temporary agent not found"));

        temporaryAgentRepository.delete(tempAgent);

        emailService.sendEmail(tempAgent.getEmail(), "Rejection Notification",
                "Your agent profile has been rejected.");
    }

    @Override
    public List<TemporaryAgent> getAllPendingAgents() {
        return temporaryAgentRepository.findAll();
    }


    @Override
    public Agent loginAgent(String username, String password) throws Exception {
        try {
            logger.info("Attempting to login agent with username: {}", username);
            Optional<Agent> optionalAgent = agentRepository.findByUserName(username);

            if (optionalAgent.isEmpty()) {
                logger.warn("Invalid login attempt for username: {}", username);
                throw new IllegalArgumentException("Invalid username or password.");
            }

            Agent agent = optionalAgent.get();

            // Compare raw password with the encoded password
            if (!passwordEncoder.matches(password, agent.getPassword())) {
                logger.warn("Invalid login attempt for username: {}", username);
                throw new IllegalArgumentException("Invalid username or password.");
            }

            logger.info("Agent with username: {} logged in successfully.", username);
            return agent;
        } catch (Exception e) {
            logger.error("Error during agent login: {}", e.getMessage(), e);
            throw new Exception("Error during agent login: " + e.getMessage(), e);
        }
    }

    @Override
    public Agent updateAgent(Long id, Agent updatedAgent, MultipartFile profilePicture) throws Exception {
        try {
            logger.info("Attempting to update agent with ID: {}", id);

            // Validate agent data
            validateAgentData(updatedAgent);

            Agent existingAgent = agentRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Agent not found."));

            if (profilePicture != null && !isValidImageType(profilePicture)) {
                throw new IllegalArgumentException("Invalid profile picture type. Only PNG, JPG, and JPEG are allowed.");
            }

            // Update fields
            existingAgent.setFullName(updatedAgent.getFullName());
            existingAgent.setEmail(updatedAgent.getEmail());
            existingAgent.setMobileNo(updatedAgent.getMobileNo());
            existingAgent.setBio(updatedAgent.getBio());
            existingAgent.setExperience(updatedAgent.getExperience());
            existingAgent.setProfilePicture(profilePicture != null ? profilePicture.getBytes() : existingAgent.getProfilePicture());
            existingAgent.setUpdatedAt(Timestamp.from(Instant.now()));

            // Update password only if a new one is provided
            if (updatedAgent.getPassword() != null && !updatedAgent.getPassword().isEmpty()) {
                logger.info("Updating password for agent with ID: {}", id);
                existingAgent.setPassword(passwordEncoder.encode(updatedAgent.getPassword()));
            }

            Agent savedAgent = agentRepository.save(existingAgent);
            logger.info("Agent with ID: {} updated successfully.", id);
            return savedAgent;
        } catch (Exception e) {
            logger.error("Error updating agent with ID: {}: {}", id, e.getMessage(), e);
            throw new Exception("Error updating agent: " + e.getMessage(), e);
        }
    }


    @Override
    public void deleteAgent(Long id) throws Exception {
        try {
            logger.info("Attempting to delete agent with ID: {}", id);
            Agent agent = agentRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Agent not found."));
            agentRepository.delete(agent);
            logger.info("Agent with ID: {} deleted successfully.", id);
        } catch (Exception e) {
            logger.error("Error deleting agent with ID: {}: {}", id, e.getMessage(), e);
            throw new Exception("Error deleting agent: " + e.getMessage(), e);
        }
    }

    @Override
    public void logoutAgent(Long id) throws Exception {
        try {
            // Placeholder for logout logic, e.g., invalidating a session
            logger.info("Attempting to log out agent with ID: {}", id);
            // Logout logic goes here (e.g., invalidate session or token)
            logger.info("Agent with ID: {} logged out successfully.", id);
        } catch (Exception e) {
            logger.error("Error logging out agent with ID: {}: {}", id, e.getMessage(), e);
            throw new Exception("Error logging out agent: " + e.getMessage(), e);
        }
    }

    @Override
    public Agent getAgentById(Long id) throws Exception {
        try {
            logger.info("Fetching agent details with ID: {}", id);
            return agentRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Agent not found."));
        } catch (Exception e) {
            logger.error("Error fetching agent with ID: {}: {}", id, e.getMessage(), e);
            throw new Exception("Error fetching agent: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Agent> getAllAgents() {
        try {
            logger.info("Fetching all agents.");
            return agentRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching all agents: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching all agents: " + e.getMessage(), e);
        }
    }

    @Override
    public void changeAgentPassword(Long id, String oldPassword, String newPassword) throws Exception {
        try {
            logger.info("Attempting to change password for agent with ID: {}", id);
            Agent agent = agentRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Agent not found."));

            // Check if old password matches the stored encoded password
            if (!passwordEncoder.matches(oldPassword, agent.getPassword())) {
                logger.warn("Old password does not match for agent with ID: {}", id);
                throw new IllegalArgumentException("Old password is incorrect.");
            }

            // Encode new password before saving
            agent.setPassword(passwordEncoder.encode(newPassword));
            agent.setUpdatedAt(Timestamp.from(Instant.now()));
            agentRepository.save(agent);
            logger.info("Password changed successfully for agent with ID: {}", id);
        } catch (Exception e) {
            logger.error("Error changing password for agent with ID: {}: {}", id, e.getMessage(), e);
            throw new Exception("Error changing password: " + e.getMessage(), e);
        }
    }

    private boolean isValidImageType(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (contentType.equals("image/png") || contentType.equals("image/jpeg") || contentType.equals("image/jpg"));
    }

    // Validation method for agent data
    private void validateAgentData(Agent agent) {
        if (agent.getUserName() == null || !ValidationClass.USERNAME_PATTERN.matcher(agent.getUserName()).matches()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (agent.getEmail() == null || !ValidationClass.EMAIL_PATTERN.matcher(agent.getEmail()).matches()) {
            throw new IllegalArgumentException("Email is not valid.");
        }
        if (agent.getMobileNo() == null || !ValidationClass.PHONE_PATTERN.matcher(agent.getMobileNo()).matches()) {
            throw new IllegalArgumentException("Mobile number should be 10 digits.");
        }
    }
}
