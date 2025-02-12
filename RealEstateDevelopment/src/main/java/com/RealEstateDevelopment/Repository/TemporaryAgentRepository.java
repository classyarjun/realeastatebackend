package com.RealEstateDevelopment.Repository;

import com.RealEstateDevelopment.Entity.TemporaryAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemporaryAgentRepository extends JpaRepository<TemporaryAgent, Long> {
    boolean existsByEmail(String email);
    boolean existsByUserName(String userName);
}
