package com.bookmind.repository;

import com.bookmind.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Custom query methods can be defined here if needed
    // For example:
    // List<Category> findByNameContaining(String name);

}
