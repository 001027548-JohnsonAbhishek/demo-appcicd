package com.user.api.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.amazonaws.services.s3.model.ObjectMetadata;

@Entity
@Table(name = "imageDetail")
public class Image {
	
	@Id
	private String id;	
	private String fileName;
	private String url;
	private String uploadDate;
	private String userId;
	private String metadata;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUploadDate() {
		return uploadDate;
	}
	public void setUploadDate(String uploadDate) {
		this.uploadDate = uploadDate;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getMetadata() {
		return metadata;
	}
	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}

}
