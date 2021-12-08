package com.user.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.user.api.model.User;

@SpringBootTest
class UserApplicationTests {

	@Test
	void contextLoads() {
		
	}

	@Test
	void assertUserLastName() {
		User user=new User();
		String firstName="Abcd";
		user.setFirstName(firstName);
		assert(user.getFirstName().equals(firstName));
	}
	
}
