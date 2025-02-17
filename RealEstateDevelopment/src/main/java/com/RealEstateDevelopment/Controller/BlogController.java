package com.RealEstateDevelopment.Controller;

import com.RealEstateDevelopment.Entity.Blog;
import com.RealEstateDevelopment.Service.BlogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/blog")
@CrossOrigin("*")
public class BlogController {
    private static final Logger logger = LoggerFactory.getLogger(BlogController.class);

    @Autowired
    private BlogService blogService;



    @PostMapping(value = "/saveBlog", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> saveBlog(@RequestPart("blog") String blogJson,
                                                        @RequestParam("image") MultipartFile image) {
        Map<String, Object> response = new HashMap<>();
        try {
            // JSON string ko Blog object me convert karo
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule()); // Java 8 Date/Time Support

            Blog blog = objectMapper.readValue(blogJson, Blog.class);

            Blog savedBlog = blogService.saveBlog(blog, image);
            logger.info("Blog saved successfully with ID: {}", savedBlog.getId());

            response.put("status", "success");
            response.put("message", "Blog saved successfully");
            response.put("blog", savedBlog);

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IOException e) {
            logger.error("Error while saving blog image: {}", e.getMessage());

            response.put("status", "error");
            response.put("message", "Error while saving blog image");
            response.put("error", e.getMessage());

            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Error while saving blog: {}", e.getMessage());

            response.put("status", "error");
            response.put("message", "Error while saving blog");
            response.put("error", e.getMessage());

            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @GetMapping("/getBlogById/{id}")
    public ResponseEntity<Blog> getBlogById(@PathVariable Long id) {
        try {
            return blogService.getBlogById(id);

        } catch (Exception e) {
            logger.error("Error while fetching blog by ID: {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping(value = "/getAllBlog")
    public ResponseEntity<List<Blog>> getAllBlog() {
        try {
            List<Blog> blogs = blogService.getAllBlog();
            if (blogs.isEmpty()) {
                logger.info("No blogs found.");
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            logger.info("Fetched all blogs successfully.");
            return new ResponseEntity<>(blogs, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while fetching blogs: {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Blog> updateBlog(@PathVariable Long id, @RequestPart Blog blog,
                                           @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            Blog updatedBlog = blogService.updateBlog(id, blog, image);
            if (updatedBlog != null) {
                return new ResponseEntity<>(updatedBlog, HttpStatus.OK);
            } else {
                logger.warn("Blog not found for update with ID: {}", id);
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch (IOException e) {
            logger.error("Error while updating blog image: {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Error while updating blog: {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteBlog(@PathVariable Long id) {
        try {
            boolean isDeleted = blogService.deleteBlog(id);
            if (isDeleted) {
                logger.info("Blog deleted successfully with ID: {}", id);
                return new ResponseEntity<>("Blog deleted successfully", HttpStatus.OK);
            } else {
                logger.warn("Blog not found for deletion with ID: {}", id);
                return new ResponseEntity<>("Blog not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Error while deleting blog: {}", e.getMessage());
            return new ResponseEntity<>("Error while deleting blog", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}