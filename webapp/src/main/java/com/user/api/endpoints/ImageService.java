package com.user.api.endpoints;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.user.api.model.CustomUserDetails;
import com.user.api.model.Image;
import com.user.api.model.User;
import com.user.api.repository.ImageRepository;
import com.user.api.repository.UserRepository;

@RestController
@RequestMapping("/api")
public class ImageService implements UserDetailsService{

	Map<String,ObjectMetadata>metadataMap = new HashMap<>();
	
	@Autowired
	private AmazonS3 s3client;
	
	@Autowired
	private	ImageRepository imageRepo;
	
	@Autowired
	private	UserRepository userRepo;

    @Value("${cloud.aws.region.bucket.name}")
    private String bucketName;
    
    @GetMapping("/users/{username}/{fileName}")
    public Image getFile(@PathVariable("fileName") String fileName,@RequestHeader Map<String, String> headers,
    		@PathVariable("username") String username) {
 
    	
    	authenticateUser(username, headers);
    	
    	//Check if the image file belongs to the user/ if it exists
    	if(!imageRepo.existsByFileNameAndUserId(fileName,userRepo.findByUsername(username).getId()))
    		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No file Exist");
    	return imageRepo.findByUserId(userRepo.findByUsername(username).getId());
    }
    
    
    @PostMapping("/users/{username}/fileUpload")
    public Image uploadFile(@RequestPart(value = "file") MultipartFile multipartFile,@RequestHeader Map<String, String> headers,
    		@PathVariable("username") String username) throws IOException {

    	authenticateUser(username, headers);
    	
    	//Handle emptly files
    	if(multipartFile==null)
    		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please Upload a picture");
    	
    	Image image;
    	
    		//Check if the image file belongs to the user/ if it exists
    		if(imageRepo.findByUserId(userRepo.findByUsername(username).getId())!=null) {
    			deleteFileHelper(bucketName,imageRepo.findByUserId(userRepo.findByUsername(username).getId()).getFileName());
    		}    		
    	
    		//Build the image entity object
    		image = setImageData(username,multipartFile);


    	return imageRepo.save(image);
    }

    
    @DeleteMapping("/users/{username}/{fileName}")
    public String deleteFile(@PathVariable("username") String username,@RequestHeader Map<String, String> headers,
    		@PathVariable("fileName") String fileName) throws IOException {

    	authenticateUser(username, headers);
    	
    	//Check if the image file belongs to the user/if it exists
    	if(!imageRepo.existsByFileNameAndUserId(fileName,userRepo.findByUsername(username).getId()))
    		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image Not Found");
    	
    	 //The function is used to delete object from ImageRepo and S3 bucket
    	 deleteFileHelper(bucketName,fileName);
    	
    	
    	return fileName + " Deleted Successfully";
    }

    
    private void deleteFileHelper(String bucketName, String fileName) {
    	imageRepo.delete(imageRepo.findByFileName(fileName));
    	s3client.deleteObject(bucketName, fileName);
    }
    
    private Image setImageData(String username,MultipartFile multipartFile) throws IOException{
    	
    	Image image = new Image();
    	
    	//Build the image entity object
    	String fileName = "";
    	String url = "";
    	String id = UUID.randomUUID().toString();
    	String userId = userRepo.findByUsername(username).getId();
    	String uploadDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

		File file = convertMultiPartToFile(multipartFile);
		fileName = username+"_"+multipartFile.getOriginalFilename();
		checkImageFormat(fileName);
	    url = bucketName + "/" + userId + "/" + fileName;

		
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(file.getTotalSpace());
		metadata.setContentLanguage("Picture");
		metadata.setServerSideEncryption("kms");
		
		uploadFileTos3bucket(fileName, file);

		image.setFileName(fileName);
    	image.setId(id);
    	image.setUserId(userId);
    	image.setUploadDate(uploadDate);
    	image.setMetadata(metadata.toString());
    	image.setUrl(url);
    	retrieveUserMetadataForImage(id,metadata);
    	file.delete();
    	
    	return image;
    }
    
    private void retrieveUserMetadataForImage(String id, ObjectMetadata metadata) {
    	metadataMap.put(id,metadata);
    }
    
    private File convertMultiPartToFile(MultipartFile file) throws IOException {
    	File newFile = new File(file.getOriginalFilename());
    	FileOutputStream os = new FileOutputStream(newFile);
    	os.write(file.getBytes());
    	os.close();
    	return newFile;
    }


    private String uploadFileTos3bucket(String fileName, File file) {
    	//Upload the file to S3 bucket
    	try {
    		s3client.putObject(new PutObjectRequest(bucketName, fileName, file));
    	}catch(AmazonServiceException e) {
    		return "Upload to S3 failed :" + e.getMessage(); 
    	}
    	return "Upload Successfull";
    }
    
    public void checkImageFormat(String fileName) {
    	
    	String format = fileName.substring(fileName.indexOf("."));
    	//Check for format
    	if(format.equals("png")||format.equals("jpg")||format.equals("jpeg")||format.equals("gif"))
    		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image Not in valid format");
    	
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
		 	User user = userRepo.findByUsername(emailId);
	        if(user==null) throw new UsernameNotFoundException("User with given emailId does not exist");
	        else return new CustomUserDetails(user);
	    }
	
}
