package com.egg.electricity_store.services;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.egg.electricity_store.entities.Image;
import com.egg.electricity_store.exceptions.MyException;
import com.egg.electricity_store.repositories.ImageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageService {
    // -> Constructor injection with @RequiredArgsConstructor
    private final ImageRepository imageRepository;

    @Transactional
    public Image create(MultipartFile imageFile) throws MyException {
        validateImageFile(imageFile);
        try {
            // Builder pattern with Lombok          
            Image newImage = Image.builder()
            .imageName(imageFile.getOriginalFilename())
            .imageMime(imageFile.getContentType())
            .imageContent(imageFile.getBytes())
            .build();
            return imageRepository.save(newImage);
        } catch (IOException e) {
            // Catch low-level exceptions and wrap them in custom exceptions (e.g., MyException)
            // Avoid exposing low-level details (like IOException, SQLException) to higher layers.
            throw new MyException("Error while saving the new image: " + e.getMessage());
        }

    }

    @Transactional(readOnly = true)
    public List<Image> getAll(){
        return imageRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Image getById(Long imageId) throws MyException {
        validateId(imageId);
        return imageRepository.findById(imageId)
            .orElseThrow(() -> new MyException("Image not found with ID: " + imageId));
    }

    public Image update(Long imageId, MultipartFile imageFile) throws MyException {

        validateId(imageId);
        validateImageFile(imageFile);

        Image image = imageRepository.findById(imageId)
                // Catch low-level exceptions and wrap them in custom exceptions (e.g. MyException)
                // Avoid exposing low-level details (like IOException, SQLException) to higher layers.
                .orElseThrow(() -> new MyException("Image not found with ID: " + imageId));

        image.setImageName(imageFile.getOriginalFilename());
        image.setImageMime(imageFile.getContentType());
        try {
            image.setImageContent(imageFile.getBytes());
        } catch (IOException e) {
            throw new MyException("Error while updating the image: " + e.getMessage());
        }
        // Persiste en BD
        return image; // No save() needed, Hibernate auto-manages changes for existing entities
    }

    @Transactional
    public void deleteById(Long imageId) throws MyException {
        validateId(imageId);

        if (!imageRepository.existsById(imageId)) {
            throw new MyException("Image not found with ID: " + imageId);
        }
        
        imageRepository.deleteById(imageId);

    }

    private void validateId(Long id) throws MyException {
        if (id == null || id <= 0) {
            throw new MyException("Invalid ID: must be greater than 0.");
        }
    }

    private void validateImageFile(MultipartFile imageFile) throws MyException {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new MyException("The image file is required.");
        }
    }
}
