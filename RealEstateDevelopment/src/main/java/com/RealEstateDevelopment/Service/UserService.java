package com.RealEstateDevelopment.Service;

import com.RealEstateDevelopment.Entity.User;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface UserService {

    String registerTemporaryUser(User user);

    String verifyOtpToRegister(String email, String otp);

    User loginUser(String username, String password) throws Exception;

    @Transactional
    User updateUserDetails(Long userId, User user);

    void deleteUser(Long id) throws Exception;

    User getUserById(Long id) throws Exception;

    List<User> getAllUsers();

    void changePassword(Long id, String oldPassword, String newPassword, String confirmPassword) throws Exception;

    void logoutUser(String username) throws Exception;

    @Transactional
    String deleteProfilePicture(Long userId);
}
