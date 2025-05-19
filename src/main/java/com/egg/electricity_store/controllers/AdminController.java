package com.egg.electricity_store.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.egg.electricity_store.exceptions.MyException;
import com.egg.electricity_store.services.AppUserService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    // -> Constructor injection with @RequiredArgsConstructor
    private final AppUserService appUserService;

    // Show the Admin Dashboard
    @GetMapping("/dashboard")
    public String adminPanel() {
        return "admin-dashboard.html";
    }

    // Read/List users
    @GetMapping("/users")
    public String list(ModelMap model) {
        model.addAttribute("users", appUserService.getAll());
        return "user-list.html";
    }

    // Change role ADMIN/USER
    @PostMapping("/turn-role/{id}")
    public String turnUserRole(@PathVariable("id") Long appUserId, RedirectAttributes redirAtt) {
        try {
            appUserService.turnRole(appUserId);
            redirAtt.addFlashAttribute("successMessage", "User role changed successfully.");
        } catch (MyException e) {
            redirAtt.addFlashAttribute("errorMessage", "Error changing user role: " + e.getMessage());
        }
        return "redirect:../users";
    }

    // Update User GET Method
    @GetMapping("/user-update/{id}")
    public String showUpdateForm(@PathVariable Long id, ModelMap model){ 
        try {
            // Add user object to the view, to show its current values by default
            model.addAttribute("user", appUserService.getById(id));
            // Send actionUrl to the form for redirecting to the POST method processUpdate
            model.addAttribute("actionUrl", "/admin/user-update/" + id); // 👈 URL for form action
            return "user-modification.html";
        } catch (MyException e) {
            model.addAttribute("errorMessage", "Error getting the user: " + e.getMessage());
            return "redirect:../users";
        }
    }

    // Update User POST Method
    @PostMapping("/user-update/{id}")
    public String processUpdate(
            @PathVariable Long id,
            @RequestParam(required = false)  String name,
            @RequestParam(required = false)  String lastName,
            @RequestParam(required = false)  String email,
            @RequestParam(required = false)  String password,
            @RequestParam(required = false)  String password2,
            MultipartFile imageFile,
            RedirectAttributes redirAtt) {
        try {
            appUserService.update(id, email, name, lastName, password, password2, imageFile);
            redirAtt.addFlashAttribute("successMessage", "User was updated successfully.");
            return "redirect:../users";
        } catch (MyException e) {
            redirAtt.addFlashAttribute("errorMessage", "Error when updating user: " + e.getMessage());
            // Redirect to the GET method to show the form with the error
            return " redirect:../user-update/"+id;
        }
    }

    // Delete
    @PostMapping("/user-delete/{id}")
    public String deleteUSer(@PathVariable("id") Long appUserId, RedirectAttributes redirAtt) {
        try {
            appUserService.deleteById(appUserId);
            redirAtt.addFlashAttribute("successMessage", "User deleted successfully.");
        } catch (MyException e) {
            redirAtt.addFlashAttribute("errorMessage", "Error when deleting user: " + e.getMessage());
        }
        return "redirect:../users";
    }
}
