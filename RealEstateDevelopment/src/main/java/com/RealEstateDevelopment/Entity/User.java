package com.RealEstateDevelopment.Entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String fullname;
    private String email;
    private String password;
    private String confirmPassword;
    private String mobileNo;
    private String address;
    private String gender;
    
    @Enumerated(EnumType.STRING)
    private Role role;
    
    @Lob
    @Column(name = "profile_picture",columnDefinition = "LONGBLOB")
    @Basic(fetch = FetchType.LAZY)
    @Nullable
    private byte[] profilePicture;
    
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    @Enumerated(EnumType.STRING)
    private Status status;

    private boolean verified = false;

    @OneToOne
    @Transient
    private ForgotPasswordOtp forgotPasswordOtp;

}
