package com.bookmind.repository;

import com.bookmind.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    //Additional methods for User can be defined here if needed
}
