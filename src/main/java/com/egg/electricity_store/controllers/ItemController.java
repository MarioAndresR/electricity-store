package com.egg.electricity_store.controllers;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.egg.electricity_store.exceptions.MyException;
import com.egg.electricity_store.services.FactoryService;
import com.egg.electricity_store.services.ItemService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/item")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final FactoryService factoryService;

    // Create
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/register")
    public String register(ModelMap model) {
        // To reuse the HTML view for creating and update, send the item object via
        // model
        model.addAttribute("item", null);
        // Send the list of existing factories as an object via model
        model.addAttribute("factories", factoryService.getAll());
        return "item-form.html";
    }

    @PostMapping("/registration")
    @PreAuthorize("hasRole('ADMIN')")
    public String registration(
            @RequestParam String itemName,
            @RequestParam String itemDescription,
            @RequestParam Long factoryId,
            ModelMap model) {
        try {
            itemService.create(itemName, itemDescription, factoryId);
            model.put("successMessage", "New item was created successfully.");
            return "index.html";
        } catch (MyException e) {
            model.addAttribute("factories", factoryService.getAll());
            Logger.getLogger(ItemController.class.getName()).log(Level.SEVERE, null, e);
            model.put("errorMessage", e.getMessage());
            return "item-form.html";
        }
    }

    // Read
    @GetMapping("/list")
    public String list(ModelMap model) {
        model.addAttribute("items", itemService.getAll());
        return "item-list.html";
    }

    // Update
    @GetMapping("/update/{id}") // Improvement for the next time: change method name to showUpdateForm()
    @PreAuthorize("hasRole('ADMIN')")
    public String update(@PathVariable Long id, ModelMap model) {
        try {
            // get the certain item to pre-view its data in the form
            model.addAttribute("item", itemService.getById(id));
            // Send the list of existing factories as an object via model
            model.addAttribute("factories", factoryService.getAll());
        } catch (MyException e) {
            Logger.getLogger(ItemController.class.getName()).log(Level.SEVERE, null, e);
            model.put("errorMessage", e.getMessage());
        }
        return "item-form.html";
    }

    @PostMapping("/update/{id}") // Improvement for the next time: change method name to processUpdate()
    @PreAuthorize("hasRole('ADMIN')")
    public String update(
            @PathVariable Long id,
            @RequestParam String itemName,
            @RequestParam String itemDescription,
            @RequestParam Long factoryId,
            RedirectAttributes redirAtt) {
        try {
            itemService.update(id, itemName, itemDescription, factoryId);
            redirAtt.addFlashAttribute("successMessage", "Item was updated successfully.");
            // Redirects to the list view with the object updated.
            return "redirect:../list";
        } catch (MyException e) {
            Logger.getLogger(ItemController.class.getName()).log(Level.SEVERE, null, e);
            redirAtt.addFlashAttribute("errorMessage", e.getMessage());
            return "item-form.html";
        }
    }

    // Delete
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteItem(@PathVariable("id") Long itemId, RedirectAttributes redirAtt) {
        try {
            itemService.deleteById(itemId);
            redirAtt.addFlashAttribute("successMessage", "Item deleted successfully.");
        } catch (MyException e) {
            redirAtt.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/item/list";
    }
}
