package com.RealEstateDevelopment.Repository;

import com.RealEstateDevelopment.Entity.Admin;
import com.RealEstateDevelopment.Entity.Agent;
import com.RealEstateDevelopment.Entity.ForgotPasswordOtp;
import com.RealEstateDevelopment.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ForgotPasswordOtpRepository extends JpaRepository<ForgotPasswordOtp, Long> {


    Optional<ForgotPasswordOtp> findByOtp(String otp);

    @Query("SELECT o FROM ForgotPasswordOtp o WHERE o.user = :user OR o.admin = :admin OR o.agent = :agent")
    ForgotPasswordOtp findByUserOrAdminOrAgent(@Param("user") User user, @Param("admin") Admin admin, @Param("agent") Agent agent);

}