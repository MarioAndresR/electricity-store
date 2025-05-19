package com.egg.electricity_store.controllers;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.egg.electricity_store.entities.Factory;
import com.egg.electricity_store.exceptions.MyException;
import com.egg.electricity_store.services.FactoryService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/factory")
@RequiredArgsConstructor
public class FactoryController {
    private final FactoryService factoryService;

    // Create
    @GetMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public String register(ModelMap model) {
        // To reuse the HTML view, send the factory object via model
        model.addAttribute("factory", null);
        return "factory-form.html";
    }

    @PostMapping("/registration")
    @PreAuthorize("hasRole('ADMIN')")
    public String registration(@RequestParam String factoryName, ModelMap model) {
        try {
            factoryService.create(factoryName);
            model.put("successMessage", "Factory was created successfully.");
            return "index.html";
        } catch (MyException e) {
            Logger.getLogger(FactoryController.class.getName()).log(Level.SEVERE, null, e);
            model.put("errorMessage", e.getMessage());
            return "factory-form.html";
        }        
    }

    // Read
    @GetMapping("/list")
    public String list(ModelMap model) {
        List<Factory> factories = factoryService.getAll();
        model.addAttribute("factories", factories);
        return "factory-list.html";
    }
    
    // Update
    @GetMapping("/update/{id}") // Improvement for the next time: change method name to showUpdateForm()
    @PreAuthorize("hasRole('ADMIN')")
    public String update(@PathVariable Long id, ModelMap model) {
        try {
            // get the certain factory to pre-view its data in the form
            model.addAttribute("factory", factoryService.getById(id));
        } catch (MyException e) {
            Logger.getLogger(FactoryController.class.getName()).log(Level.SEVERE, null, e);
            model.put("errorMessage", e.getMessage());
        }
        return "factory-form.html";
    }

    @PostMapping("/update/{id}") // Improvement for the next time: change method name to processUpdate()
    @PreAuthorize("hasRole('ADMIN')")
    public String update(@PathVariable Long id, @RequestParam String factoryName, RedirectAttributes redirAtt){
        try {
            factoryService.update(id, factoryName);
            redirAtt.addFlashAttribute("successMessage", "Factory was updated successfully.");
            // Redirects to the list view with the object updated.
            return "redirect:../list";
        } catch (MyException e) {
            Logger.getLogger(FactoryController.class.getName()).log(Level.SEVERE, null, e);
            redirAtt.addFlashAttribute("errorMessage", e.getMessage());
            return "factory-form.html";
        }
    }

    // Delete
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteFactory(@PathVariable("id") Long factoryId, RedirectAttributes redirAtt) {
        try {
            factoryService.deleteById(factoryId);
            redirAtt.addFlashAttribute("successMessage", "Factory deleted successfully.");
        } catch (MyException e) {
            redirAtt.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/factory/list";
    }
}
