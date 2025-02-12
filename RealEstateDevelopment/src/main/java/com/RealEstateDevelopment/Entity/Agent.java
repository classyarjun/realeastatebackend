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
@Table(name = "agents")
public class Agent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userName;
    private String fullName;
    private String email;
    private String password;
    private String mobileNo;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Lob
    @Column(name = "profile_picture", columnDefinition = "LONGBLOB")
    @Basic(fetch = FetchType.LAZY)
    private byte[] profilePicture;

    private double experience;
    private double rating;
    private String bio;

    private Timestamp createdAt;
    private Timestamp updatedAt;

    @Enumerated(EnumType.STRING)
    private Status status;

    private boolean isApproved = true; // Approval status from admin
}
