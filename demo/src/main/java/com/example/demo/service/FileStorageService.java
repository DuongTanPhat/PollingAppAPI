package com.example.demo.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.exception.FileStorageException;
import com.example.demo.exception.MyFileNotFoundException;
import com.example.demo.model.User;
import com.example.demo.property.FileStorageProperties;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.UserPrincipal;

@Service
public class FileStorageService {
	@Autowired
	private UserRepository userRepository;
	private final Path fileStorageLocation;
	@Autowired
	public FileStorageService(FileStorageProperties fileStorageProperties) {
		this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
				.toAbsolutePath().normalize();
		try {
			Files.createDirectories(this.fileStorageLocation);
		}catch(Exception ex) {
			throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
		}
	}
	public String storeFile(MultipartFile file, UserPrincipal currentUser) {
		System.out.println("0");
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());
		System.out.println("0");
		String newFileName = "";
		try {
			System.out.println("0");
			if(fileName.contains("..")) {
				throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
				
			}
			System.out.println("0");
			String fileExtension = "";
			try {
				System.out.println("0");
				fileExtension = fileName.substring(fileName.lastIndexOf("."));
			}catch(Exception e) {
				fileExtension ="";
			}
			System.out.println(currentUser);
			newFileName = currentUser.getUsername()+fileExtension;
			System.out.println("0");
			Path targetLocation = this.fileStorageLocation.resolve(newFileName);
			System.out.println(targetLocation.toString());
			Files.copy(file.getInputStream(), targetLocation,StandardCopyOption.REPLACE_EXISTING);
			System.out.println("0");
			User user = userRepository.getOne(currentUser.getId());
			user.setPhoto(newFileName);
			System.out.println("0");
			userRepository.save(user);
			System.out.println("0");
			return newFileName;
		}catch(IOException ex) {
			throw new FileStorageException("Could not store file " + newFileName + ". Please try again!", ex);
		}
	}
	public Resource loadFileAsResource(String fileName) {
		try {
			Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
			Resource resource = new UrlResource(filePath.toUri());
			if(resource.exists()) {
				return resource;
			}
			else {
				throw new MyFileNotFoundException("File not found " + fileName);
			}
		}catch(MalformedURLException ex) {
			throw new MyFileNotFoundException("File not found " + fileName, ex);
		}
	}
}
