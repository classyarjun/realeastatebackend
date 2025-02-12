package com.RealEstateDevelopment.Service;

import com.RealEstateDevelopment.Entity.Blog;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface BlogService {
    public Blog saveBlog(Blog blog, MultipartFile imagePath) throws IOException;
    public ResponseEntity<Blog> getBlogById(@PathVariable Long id);
    public List<Blog> getAllBlog();
    public Blog updateBlog(@PathVariable Long id, Blog blog, MultipartFile imagePath) throws IOException;
    public boolean deleteBlog(@PathVariable Long id);






}
