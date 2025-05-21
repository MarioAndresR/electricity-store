package com.egg.electricity_store.initializers;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import com.egg.electricity_store.entities.Image;
import com.egg.electricity_store.exceptions.MyException;
import com.egg.electricity_store.repositories.ImageRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
/**
 * Initializes a default image in the database at application startup.
 * Ensures a fallback image is available for users who do not upload one when creating an account.
 * Uses @PostConstruct to execute the logic after Spring initializes the bean.
 */
@Component
@RequiredArgsConstructor
public class DefaultImageInitializer {
    private final ImageRepository imageRepository;

    @PostConstruct
    public void insertDefaultImage() throws MyException {
        try {
            // Check if the default image already exists in DB
            if (!imageRepository.existsById(1L)) {
                // Load default image from resources
                ClassPathResource imgFile = new ClassPathResource("static/img/default.jpg");
                byte[] imageBytes = StreamUtils.copyToByteArray(imgFile.getInputStream());
                
                // Create Image entity
                Image defaultImage = new Image();
                defaultImage.setImageName("default.jpg");
                defaultImage.setImageMime("image/jpeg");
                defaultImage.setImageContent(imageBytes);
                
                // Save to database
                imageRepository.save(defaultImage);
            }
        } catch (IOException e) {
            throw new MyException("Failed to load default image: " + e.getMessage());
        }
    }
}
