package com.egg.electricity_store.controllers;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.egg.electricity_store.entities.Image;
import com.egg.electricity_store.exceptions.MyException;
import com.egg.electricity_store.services.AppUserService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping("/image")
@RequiredArgsConstructor
public class ImageController {

    // -> Constructor injection with @RequiredArgsConstructor
    private final AppUserService appUserService;

    @GetMapping("/profile/{id}")
    public ResponseEntity<byte[]> userImage(@PathVariable Long id) {
        try {
            Image userImg = appUserService.getById(id).getImage();

            if (userImg == null || userImg.getImageContent() == null) {
                // Return the default profile image here
                return getDefaultImage();
            }

            // HTTP response headers with the image parameters
            HttpHeaders responseHeaders = new HttpHeaders();
            // Set content type (image/png, image/jpeg, etc.)
            responseHeaders.setContentType(MediaType.parseMediaType(userImg.getImageMime()));
            // Specifies the size of the response in bytes. Not mandatory but
            // recommended for performance optimization.
            responseHeaders.setContentLength(userImg.getImageContent().length);

            return new ResponseEntity<>(userImg.getImageContent(), responseHeaders, HttpStatus.OK);

        } catch (MyException e) {
            // Shorthand for returning an HTTP 404 Not Found response. (not user found)
            return ResponseEntity.notFound().build();
        }

    }

    private ResponseEntity<byte[]> getDefaultImage() {
        try {
            InputStream in = getClass().getResourceAsStream("/static/img/default.jpg");
            if (in == null)
                return ResponseEntity.noContent().build();

            byte[] imageBytes = in.readAllBytes();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentLength(imageBytes.length);
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.noContent().build();
        }
    }

}
