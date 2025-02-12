package com.RealEstateDevelopment.Repository;

import com.RealEstateDevelopment.Entity.PendingProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PendingPropertyRepository extends JpaRepository<PendingProperty, Long> {

    // Correct method signature
    List<PendingProperty> findByAgent_Id(Long agentId);

    Optional<PendingProperty> findById(Long propertyId);

}

