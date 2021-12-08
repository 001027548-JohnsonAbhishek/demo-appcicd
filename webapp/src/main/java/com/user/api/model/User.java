package com.user.api.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

@Entity
@Table(name = "userDetail")
public class User {
	
	@Id
	private String id;	
	private String firstName;
	private String lastName;
	
	@JsonProperty(access = Access.WRITE_ONLY)
	private String password;
	
	private String username;
	@JsonIgnore
	private String accountCreated;
	@JsonIgnore
	private String accountUpdated;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstname) {
		this.firstName = firstname;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastname) {
		this.lastName = lastname;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getAccountCreated() {
		return accountCreated;
	}
	public void setAccountCreated(String accountcreated) {
		this.accountCreated = accountcreated;
	}
	public String getAccountUpdated() {
		return accountUpdated;
	}
	public void setAccountUpdated(String accountupdated) {
		this.accountUpdated = accountupdated;
	}

	

}
