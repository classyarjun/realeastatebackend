package com.RealEstateDevelopment.Repository;

import com.RealEstateDevelopment.Entity.TemporaryUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemporaryUserRepository extends JpaRepository<TemporaryUser, Long> {
    Optional<TemporaryUser> findByEmail(String email);
    Optional<TemporaryUser> findByOtp(String otp);

}