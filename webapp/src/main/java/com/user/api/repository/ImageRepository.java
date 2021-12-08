package com.user.api.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.user.api.model.Image;


public interface ImageRepository extends JpaRepository<Image, Integer>{
	Image findByUserId(String userId);
	Image findByFileName(String fileName);
	Boolean existsByFileName(String fileName);
	Boolean existsByFileNameAndUserId(String fileName,String userId);
	Image findById(String id);
}
