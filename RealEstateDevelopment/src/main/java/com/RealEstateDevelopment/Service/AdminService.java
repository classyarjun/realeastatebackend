package com.RealEstateDevelopment.Service;

import com.RealEstateDevelopment.Entity.Admin;
import com.RealEstateDevelopment.Entity.PropertyNew;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdminService {
    String registerAdmin(Admin admin);
    String loginAdmin(String username, String password);
    void logoutAdmin(String username);
    void deleteAdmin(Long adminId);
    Admin getAdminById(Long adminId);
    Admin getAdminByUsername(String username);
    List<Admin> getAllAdmins();
    String updateAdmin(Long adminId,Admin admin);
    PropertyNew updateAgentAndProperty(Long propertyId, PropertyNew updatedProperty, List<MultipartFile> newImages);
}
