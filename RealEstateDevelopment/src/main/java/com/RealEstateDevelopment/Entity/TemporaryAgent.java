package com.RealEstateDevelopment.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "temporary_agents")
public class TemporaryAgent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String userName;
    private String fullName;
    private String email;
    private String password;
    private String mobileNo;
    private double experience;
    private double rating;
    private String bio;

    @Lob
    @Column(name = "profile_picture", columnDefinition = "LONGBLOB")
    private byte[] profilePicture;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Timestamp createdAt;
    private Timestamp updatedAt;
    private boolean isApproved = false;
}
