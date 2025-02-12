package com.RealEstateDevelopment.Repository;

import com.RealEstateDevelopment.Entity.PropertyNew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyNewRepository extends JpaRepository<PropertyNew, Long>, JpaSpecificationExecutor<PropertyNew> {

    List<PropertyNew> findByAgent_Id(Long agentId);

    List<PropertyNew> findByTitleContainingIgnoreCaseOrAddressContainingIgnoreCase(String title, String address);

}
