package com.egg.electricity_store.services;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.egg.electricity_store.entities.AppUser;
import com.egg.electricity_store.entities.Image;
import com.egg.electricity_store.enumerations.Role;
import com.egg.electricity_store.repositories.AppUserRepository;
import com.egg.electricity_store.exceptions.MyException;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * ==================================================================================
 *                      📌 loadUserByUsername - Step-by-Step Execution  
 * ==================================================================================
 * 
 * 1️⃣ User Initiates Login:  
 *    - The user submits their email and password through the authentication system.  
 * 
 * 2️⃣ Spring Security Calls loadUserByUsername(email):  
 *    - Spring Security automatically invokes this method during authentication.  
 *    - It queries the database for a user with the provided email.  
 * 
 * 3️⃣ If the User Exists:  
 *    - A UserDetails object (AppUser) is returned, containing:  
 *      ✅ Email (used as the username).  
 *      ✅ Hashed password (stored securely).  
 *      ✅ Granted authorities (roles/permissions).  
 *    - Spring Security then compares the hashed password with the entered password.  
 *    - If the passwords match, authentication succeeds, and the user gains access.  
 * 
 * 4️⃣ If the User Does NOT Exist:  
 *    - A UsernameNotFoundException is thrown.  
 *    - Spring Security automatically handles this by denying access.  
 * 
 * 📌 This method ensures secure authentication by leveraging Spring Security’s  
 * built-in mechanisms for user validation and password verification.  
 */

@Service
@RequiredArgsConstructor
public class AppUserService implements UserDetailsService {
    // -> Constructor injection with @RequiredArgsConstructor
    private final AppUserRepository appUserRepository;
    private final ImageService imageService;
    private final BCryptPasswordEncoder passwordEncoder;
    // egular expressions for email validation.
    private static final Pattern EMAIL_REGEX = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    // Regular Expression for password validation.
    private static final Pattern PASSWORD_REGEX = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=()-])(?=\\S+$).{8,20}$");

    // Spring Security already manages session authentication. 
    // No need to manually store the user in the session.
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("App User not found with email: " + email));
        
    }

    @Transactional
    public AppUser create(
        String email, 
        String name, 
        String lastName,
        String password,
        String password2,
        MultipartFile imageFile) throws MyException {
        emailExists(email);
        // Validate required params
        validateParams(email, name, lastName, password, password2);
        
        // Use provided image or fetch default from DB 
        Image image = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            // Validate image file's format and size
            valImgFileFormatSize(imageFile);
            try {
                image = imageService.create(imageFile); // Save image only if provided
            } catch (MyException e) {
                // Catches MyException from ImageService and rethrows it with more context.
                // Ensures the higher layer gets a meaningful error message.
                throw new MyException("Failed to save user image: " + e.getMessage());
            }
        } else {
            // If no image is provided, assign a default image. This image is stored in the DB
            // at application startup (handled by DefaultImageInitializer).
            image = getDefaultImage();
        }
            
        // Builder pattern with Lombok
        AppUser newAppUser = AppUser.builder()
            .email(email)
            .name(name)
            .lastName(lastName)
            .password(passwordEncoder.encode(password)) // Use injected encoder
            // .role(Role.USER) // No need, role was set by default in entity class with Lombok
            .image(image)
            .build();
        
        return appUserRepository.save(newAppUser);
    }

    @Transactional(readOnly = true)
    public List<AppUser> getAll(){
        return appUserRepository.findAll();
    }

    @Transactional(readOnly = true)
    public AppUser getById(Long appUserId) throws MyException {
        AppUser user = appUserRepository.findById(appUserId)
                .orElseThrow(() -> new MyException("App User not found with ID: " + appUserId));

        // Let the service handle lazy loading, forcely load image inside the session.
        if (user.getImage() != null) {
            user.getImage().getImageContent();
        }

        return user;
    }

    @Transactional
    public AppUser update(
            Long appUserId,
            String email,
            String name,
            String lastName,
            String password,
            String password2,
            MultipartFile imageFile) throws MyException {
        validateId(appUserId);
        // Null parameters means the user did not provide a new value. it’s allowed.
        validateUpdateParams(email, name, lastName, password, password2);

        AppUser appUser = appUserRepository.findById(appUserId)
                .orElseThrow(() -> new MyException("App User not found with ID: " + appUserId));

        // Update only the non-null fields (allow partial updates).
        if (!email.isBlank() && !email.equals(appUser.getEmail()) )
            appUser.setEmail(email);
        if (!name.isBlank() && !name.equals(appUser.getName()) )
            appUser.setName(name);
        if (!lastName.isBlank() && !lastName.equals(appUser.getLastName()) )
            appUser.setLastName(lastName);
        if (password != null && !password.isBlank() && password2 != null && !password2.isBlank())
            appUser.setPassword(passwordEncoder.encode(password));

        // Image will be updated when the user loads one
        if (imageFile != null && !imageFile.isEmpty()) {
            // Validate image file's format and size
            valImgFileFormatSize(imageFile);
            Image updImage;
            try {
                // When the user has an asociated image (not the default one)
                if (appUser.getImage() != null && appUser.getImage().getImageId() != 1L) {
                    // Update the existing image
                    updImage = imageService.update(appUser.getImage().getImageId(), imageFile);
                } else {
                    // Create a new image in DB
                    updImage = imageService.create(imageFile);
                }
            } catch (MyException e) {
                // Catches MyException from ImageService and rethrows it with more context.
                // Ensures the higher layer gets a meaningful error message.
                throw new MyException("Failed to update user image: " + e.getMessage());
            }
            appUser.setImage(updImage);
        }

        return appUser; // No save() needed, Hibernate auto-manages changes for existing entities
    }

    @Transactional
    public void deleteById(Long appUserId) throws MyException {
        validateId(appUserId);
        
        if (!appUserRepository.existsById(appUserId)) {
            throw new MyException("App User not found with ID: " + appUserId);   
        }

        appUserRepository.deleteById(appUserId);
    }

    // This method is dedicated to create admin accepting the minimal required info 
    // and using internal logic of the service
    @Transactional
    public AppUser createAdmin(String email, String name, String password) throws MyException {

        AppUser admin = AppUser.builder()
                .email(email)
                .name(name)
                .lastName("- Created by default")
                .password(passwordEncoder.encode(password))
                .role(Role.ADMIN) // Force admin role explicitly
                .image(getDefaultImage())
                .build();

        return appUserRepository.save(admin);
    }

    @Transactional
    public void turnRole(Long appUserId) throws MyException {
        AppUser appUser = appUserRepository.findById(appUserId)
                .orElseThrow(() -> new MyException("User not found"));

        if (appUser.getRole().equals(Role.ADMIN)) {
            appUser.setRole(Role.USER);
        } else if (appUser.getRole().equals(Role.USER)) {
            appUser.setRole(Role.ADMIN);
        }
        // No need to call save(), Hibernate auto-manages change
    }


    private Image getDefaultImage() throws MyException {
        try {
            return imageService.getById(1L);
        } catch (Exception e) {
           throw new MyException("Default image not found in database");
        }
    }

    private void emailExists(String email) throws MyException {
        if (appUserRepository.findByEmail(email).isPresent()) {
            throw new MyException("Email is already registered.");
        }
    }

    private void validateId(Long id) throws MyException {
        if (id == null || id <= 0) {
            throw new MyException("Invalid ID: must be greater than 0.");
        }
    }

    // All fields are required, for create method.
    private void validateParams(
        String email, 
        String name, 
        String lastName,
        String password,
        String password2) throws MyException {
        if (email == null || email.isBlank() || !EMAIL_REGEX.matcher(email).matches()) {
            throw new MyException("The email must have a valid format.");
        }
        if (name == null || name.isBlank()) {
            throw new MyException("The name field is required.");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new MyException("The last name field is required.");
        }
        if (password == null || password.isBlank()) {
            throw new MyException("The password field is required.");
        }
        if (password2 == null || password2.isBlank()) {
            throw new MyException("The password confirmation is required.");
        }
        if (!PASSWORD_REGEX.matcher(password).matches() || !PASSWORD_REGEX.matcher(password2).matches()) {
            throw new MyException("The password must be at least 8 characters and include upper and lower case letters, a number, and a special character.");
        }
        if (!password.equals(password2)) {
            throw new MyException("The passwords must be equal.");
        }
    }

    // When a parameter is not null (valid input) but blank, it throws an exception (invalid input).
    // To allow partial updates (i.e., some fields can be null if not updated).
    private void validateUpdateParams(
        String email, 
        String name, 
        String lastName, 
        String password, 
        String password2) throws MyException {
        if (email != null && (email.isBlank() || !EMAIL_REGEX.matcher(email).matches()) ) {
            throw new MyException("The email must have a valid format.");
        }
        if (name != null && name.isBlank()) {
            throw new MyException("The name field cannot be blank.");
        }
        if (lastName != null && lastName.isBlank()) {
            throw new MyException("The last name field cannot be blank.");
        }
        // Only validate password if it is provided (i.e., not empty and not blank)
        if (password != null && !password.isBlank()) {
            // 1. Format check (regexp)
            if (!PASSWORD_REGEX.matcher(password).matches()) {
                throw new MyException(
                        "Password must be 8-20 characters and include upper/lowercase letters, a number, and a special character.");
            }
            // 2. Confirm password presence
            if (password2 == null || password2.isBlank()) {
                throw new MyException("Please confirm your password.");
            }
            // 3. Match check
            if (!password.equals(password2)) {
                throw new MyException("Passwords must match.");
            }
        }
        // No else! If password is blank, simply ignore and do not update.
    }

    // Check for specific file types and a set max. size.
    private void valImgFileFormatSize(MultipartFile imageFile) throws MyException {
        // Validate file type (JPEG, PNG)
        String contentType = imageFile.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
            throw new MyException("Only JPEG and PNG images are allowed.");
        }

        // Validate file size (max 5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (imageFile.getSize() > maxSize) {
            throw new MyException("File size exceeds 5MB.");
        }
    }
}
