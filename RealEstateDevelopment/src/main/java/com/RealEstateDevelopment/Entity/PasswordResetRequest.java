package com.RealEstateDevelopment.Entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetRequest {

    private String otp;
    private String password;
    private String confirmPassword;
}