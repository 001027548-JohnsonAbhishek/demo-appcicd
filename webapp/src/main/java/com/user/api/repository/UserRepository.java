package com.user.api.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.user.api.model.User;


public interface UserRepository extends JpaRepository<User, Integer> {
	boolean existsByUsername(String username);
	User findByUsername(String username);
}
