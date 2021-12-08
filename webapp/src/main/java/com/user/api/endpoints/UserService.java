package com.user.api.endpoints;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import java.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.user.api.model.CustomUserDetails;
import com.user.api.model.User;
import com.user.api.repository.UserRepository;

@RestController
@RequestMapping("/api")
public class UserService implements UserDetailsService  {

	@Autowired
	private UserRepository repo;

	@GetMapping("/users/{username}")
	public User getUser(@PathVariable("username") String username,@RequestHeader Map<String, String> headers) {

		if (!repo.existsByUsername(username)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The User Does'nt Exists");
		}
		
		//Authenticates the credentials of a user
		authenticateUser(username , headers);
		
		return repo.findByUsername(username);
	}
	
	@PostMapping("/users")
	public User createUser(@RequestBody User user) {
		
		if( user.getAccountCreated()!=null || user.getAccountUpdated()!=null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The User is not allowed to update account updated and account created fields");
		
		// If username does'nt already exist or is not in valid format then create the new data for the user
		if (!repo.existsByUsername(user.getUsername()) && isValidUsername(user.getUsername())) {
			String pw_hash = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
			String uuid = UUID.randomUUID().toString();
			user.setId(uuid);
			user.setPassword(pw_hash);
			user.setAccountCreated(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			user.setAccountUpdated(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		} else {
			
			if(!isValidUsername(user.getUsername()))
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The Username is not in valid format");

			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The Username Already Exists");
		}

		return repo.save(user);
	}

	public boolean isValidUsername(String username) {
		return username.contains("@");
	}
	
	@PutMapping("/users/{username}")
	public User updateUser(@RequestBody User updateUserObj, @PathVariable("username") String username,@RequestHeader Map<String, String> headers) {
		
		if(updateUserObj==null)
			throw new ResponseStatusException(HttpStatus.NO_CONTENT, "The fields are empty");
				
		//Authenticates the credentials of a user
		authenticateUser(username , headers);
		User user = repo.findByUsername(username);
		
		//Check if the user is trying to update the disallowed fields
		if (!(updateUserObj.getAccountCreated() == null) || !(updateUserObj.getAccountUpdated() == null)
				|| !(updateUserObj.getUsername() == null) || !(updateUserObj.getId() == null))
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are not allowed to update some fields");

		//User updates the firstname
		if (!(updateUserObj.getFirstName() == null)) {
			user.setFirstName(updateUserObj.getFirstName());
		}

		//User updates the lastname
		if (!(updateUserObj.getLastName() == null)) {
			user.setLastName(updateUserObj.getLastName());
		}
		
		//User updates the password
		if (!(updateUserObj.getPassword() == null)) {
			String pw_hash = BCrypt.hashpw(updateUserObj.getPassword(), BCrypt.gensalt());
			user.setPassword(pw_hash);
		}
		
		user.setAccountUpdated(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

		return repo.save(user);
	}
	
	
		public void authenticateUser(String username,Map<String, String> headers) {
			
			//Retrieve the user credentials from authorization header and authenticate correct username
			headers.forEach((key, value) -> {
				if(key.contains("authorization")) {
				String str="";
				try {
					str = new String(Base64.getDecoder().decode(value.split(" ")[1]),"UTF-8");
					
					if(username.equals(str.substring(0,str.indexOf(":"))))
							loadUserByUsername(username);
					else
						throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The User credentials are not valid");
							
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				}
			});
			
		}
	
		 @Override
		    public UserDetails loadUserByUsername(String emailId) throws UsernameNotFoundException {
		        //Authenticate the user details
			 	User user = repo.findByUsername(emailId);
		        if(user==null) throw new UsernameNotFoundException("User with given emailId does not exist");
		        else return new CustomUserDetails(user);
		    }

}
