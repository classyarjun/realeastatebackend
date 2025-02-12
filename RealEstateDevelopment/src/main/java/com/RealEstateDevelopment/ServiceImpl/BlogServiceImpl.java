package com.RealEstateDevelopment.ServiceImpl;

import com.RealEstateDevelopment.Entity.Blog;
import com.RealEstateDevelopment.Repository.BlogRepository;
import com.RealEstateDevelopment.Service.BlogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Service
public class BlogServiceImpl implements BlogService {

    private static final Logger logger = LoggerFactory.getLogger(BlogService.class);

    @Autowired
    private BlogRepository blogRepository;

    public Blog saveBlog(Blog blog, MultipartFile image) throws IOException {
         String imagePath = saveImageAndGetImagePath(image);
         blog.setImagePath(imagePath);
        blog.setDate(LocalDate.now());
        Blog savedBlog = blogRepository.save(blog);
        logger.info("Blog saved successfully with ID: {}", savedBlog.getId());
        return savedBlog;
    }

    private String saveImageAndGetImagePath(MultipartFile imagePath) throws IOException {
        if (!imagePath.isEmpty()) {
            String uploadDir = "uploads/blogImages";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String uniqueFileName = UUID.randomUUID() + "_" + imagePath.getOriginalFilename();

            Path filePath = Paths.get(uploadDir, uniqueFileName);
            Files.write(filePath, imagePath.getBytes());
            return filePath.toString();
        }else
            return null;
    }
    public ResponseEntity<Blog> getBlogById(Long id) {
        Optional<Blog> blogOptional = blogRepository.findById(id);

        if (blogOptional.isPresent()) {
            return new ResponseEntity<>(blogOptional.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
    public List<Blog> getAllBlog() {
        try {
            List<Blog> blogs = blogRepository.findAll();
            logger.info("Fetched {} blogs from database.", blogs.size());
            return blogs;
        } catch (Exception e) {
            logger.error("Error while fetching blogs: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public Blog updateBlog(Long id, Blog blog, MultipartFile image) throws IOException {
        Optional<Blog> existingBlogOptional = blogRepository.findById(id);
        if (existingBlogOptional.isPresent()) {
            Blog existingBlog = existingBlogOptional.get();
            existingBlog.setTitle(blog.getTitle());
            existingBlog.setDescription(blog.getDescription());
            existingBlog.setDate(LocalDate.now());
            String imagePath = saveImageAndGetImagePath(image);
            blog.setImagePath(imagePath);

            return blogRepository.save(existingBlog);
        }
        return null;
    }
    public boolean deleteBlog(Long id) {
        if (blogRepository.existsById(id)) {
            blogRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
