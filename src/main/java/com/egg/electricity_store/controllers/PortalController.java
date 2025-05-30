package com.egg.electricity_store.controllers;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.egg.electricity_store.entities.AppUser;
import com.egg.electricity_store.exceptions.MyException;
import com.egg.electricity_store.services.AppUserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class PortalController {

     // -> Constructor injection with @RequiredArgsConstructor
     private final AppUserService appUserService;
    
    @GetMapping("/")
    public String index() {
        return "index.html";
    }

    @GetMapping("/register")
    public String register(ModelMap modelo) {
        // Send an attribute with the current path, to not show the Sign up button
        // in the user registration view (using Thymeleaf).
        modelo.addAttribute("currentPage", "register");
        return "user-registration.html";
    }

    @PostMapping("/registration")
    public String registration(
            @RequestParam String email,
            @RequestParam String name,
            @RequestParam String lastName,
            @RequestParam String password,
            @RequestParam String password2,
            @RequestParam MultipartFile imageFile,
            ModelMap model) {
        try {
            appUserService.create(email, name, lastName, password, password2, imageFile);
            model.put("successMessage", "User was created successfully.");
            return "index.html";
        } catch (MyException e) {
            Logger.getLogger(PortalController.class.getName()).log(Level.SEVERE, null, e);
            model.put("errorMessage", e.getMessage());
            // Load data in the view when an error occurs
            model.put("name", name);
            model.put("lastName", lastName);
            model.put("email", email);
            return "user-registration.html"; // Return the registration form when an error occurss.
        }
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) Boolean error, ModelMap model) {
        // 
        if (Boolean.TRUE.equals(error)) {
            model.addAttribute("errorMessage", "Invalid email or password. Please try again.");
        }
        // Send an attribute with the current path, to not show the Login button
        // in the login view (using Thymeleaf).
        model.addAttribute("currentPage", "login");
        return "login.html";
    }

    // Check if the authenticated user is Admin
    private boolean checkAdmin(Authentication auth) {
        boolean isAdmin = false;
        if (auth != null && auth.isAuthenticated()) {
            isAdmin = auth.getAuthorities()
                    .stream()
                    .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));
        }
        return isAdmin;
    }

    /* Spring Security automatically adds "ROLE_" behind the scenes when you use:
     *  - hasRole("ADMIN")
     *  - hasAnyRole("ADMIN", "USER")
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/home")
    public String home(Authentication auth) {
        if (checkAdmin(auth)) {
            // Aunthenticated with role Admin:
            return "redirect:/admin/dashboard";
        }
        return "home.html";
    }

    /*
     * ========================================================================
     *        GET & POST Methods for any User to Edit its own Profile
     * ========================================================================
     *
     */ 

    // From "Edit Profile" option in home -> header fragment
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/profile")
    public String profile(Authentication auth, ModelMap model) {     
        // Get authenticated user from SecurityContext
        AppUser user = (AppUser) auth.getPrincipal();
        // Add user attributes to the view, to show its current values by default
        model.put("user", user);
        // Send actionUrl to the form, to redirect to the POST method updateProfile
        model.addAttribute("actionUrl", "/profile/" + user.getAppUserId());// 👈 URL for form action

        return "user-modification.html";
    }

    // From user-modification.html form
    // Método POST, recibe el id del usuario y actualiza (Perfil)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/profile/{id}")
    public String updateProfile(
            @PathVariable Long id,
            @RequestParam(required = false)  String name,
            @RequestParam(required = false)  String lastName,
            @RequestParam(required = false)  String email,
            @RequestParam(required = false)  String password,
            @RequestParam(required = false)  String password2,
            MultipartFile imageFile,
            Authentication auth, // To get the authenticated user role.
            RedirectAttributes redirAtt) {
        try {
            // Current user in DB
            AppUser currUser = appUserService.getById(id);

            // Protect Users from Updating Others
            if (!auth.getName().equals(currUser.getEmail())) {
                throw new MyException("You cannot update another user's profile.");
            }

            // Udate user in DB
            AppUser updUser = appUserService.update(id, email, name, lastName, password, password2, imageFile);
            
            // Update validation
            boolean userBasicDataChanged = appUserService.userBasicDataChanged(currUser, updUser);
            boolean userPasswordChanged = appUserService.userPasswordChanged(currUser, updUser);
            boolean userImageChanged = appUserService.userImageChanged(currUser, updUser);

            // Refresh SecurityContext only when needed (if user data has actually changed)            
            if (userBasicDataChanged || userPasswordChanged || userImageChanged) {
                // Reload updated user for Spring Security
                UserDetails updatedUser = appUserService.loadUserByUsername(updUser.getEmail());
                // Refresh Spring Security context with new user (update session user)
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                updatedUser,
                                updatedUser.getPassword(),
                                updatedUser.getAuthorities()));

                redirAtt.addFlashAttribute("successMessage", "User profile updated successfully.");
            } else {
                redirAtt.addFlashAttribute("infoMessage", "No changes were detected in your profile.");
            }

            return checkAdmin(auth) ? "redirect:/admin/dashboard" : "redirect:/home";

        } catch (MyException ex) {
            redirAtt.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:../profile";
        }
    }
    
}
