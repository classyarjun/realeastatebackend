package com.RealEstateDevelopment.Repository;

import com.RealEstateDevelopment.Entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogRepository extends JpaRepository<Blog, Long> {

}
