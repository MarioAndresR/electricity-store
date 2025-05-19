package com.egg.electricity_store.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.egg.electricity_store.entities.Factory;
import com.egg.electricity_store.entities.Item;
import com.egg.electricity_store.exceptions.MyException;
import com.egg.electricity_store.repositories.ItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final FactoryService factoryService;

    @Transactional
    public Item create (
        // Integer itemNumber, Auto-generated in this method
        String itemName,
        String itemDescription,
        Long factoryId) throws MyException {
        validateParams(itemName, itemDescription, factoryId);

        Factory factory = factoryService.getById(factoryId);

        Item newItem = Item.builder()
            .itemNumber(getNextItemNumber()) // "Auto-generated" max/last value plus 1.
            .itemName(itemName)
            .itemDescription(itemDescription)
            .factory(factory)
            .build();
        return itemRepository.save(newItem);
    }

    @Transactional(readOnly = true)
    public List<Item> getAll(){
        return itemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Item getById(Long itemId) throws MyException{
        return itemRepository.findById(itemId)
            .orElseThrow(() -> new MyException("Item not found with ID: " + itemId));
    }

    @Transactional
    public Item update(
        Long itemId, 
        String itemName,
        String itemDescription,
        Long factoryId) throws MyException {
        validateId(itemId);
        validateUpdateParams(itemName, itemDescription, factoryId);
        
        Item item = getById(itemId);
        
        Factory factory = factoryService.getById(factoryId);
        
        // Update only the non-null fields (allow partial updates).
        if (itemName != null)           item.setItemName(itemName);
        if (itemDescription != null)    item.setItemDescription(itemDescription);
        if (factory != null)            item.setFactory(factory);
 
        return item; // No need to call save(), Hibernate auto-manages changes
    }

    @Transactional
    public void deleteById(Long itemId) throws MyException {
        validateId(itemId);
  
        if (!itemRepository.existsById(itemId)) {
            throw new MyException("Item not found with ID: " + itemId);
        }

        itemRepository.deleteById(itemId);
    }

    // Find the max itemNumber (0 if null) and increment by 1.
    private Integer getNextItemNumber() {
        return itemRepository.findMaxItemNumber().orElse(0) + 1;
    }

    private void validateId(Long id) throws MyException {
        if (id == null || id <= 0) {
            throw new MyException("Invalid ID: must be greater than 0.");
        }
    }

    // All fields are required, for create method.
    private void validateParams(
            String itemName,
            String itemDescription,
            Long factoryId) throws MyException {
        if (itemName == null || itemName.isBlank()) {
            throw new MyException("The name field is required.");
        }
        if (itemDescription == null || itemDescription.isBlank()) {
            throw new MyException("The description field is required.");
        }
        if (factoryId == null) {
            throw new MyException("The factory field is required.");
        }
        if (factoryId <= 0) {
            throw new MyException("Invalid factory ID: must be greater than 0.");
        }
    }

    // To allow partial updates (i.e., some fields can be null if not updated).
    private void validateUpdateParams(
            String itemName,
            String itemDescription,
            Long factoryId) throws MyException {
        if (itemName != null && itemName.isBlank()) {
            throw new MyException("The item name cannot be blank.");
        }
        if (itemDescription != null && itemDescription.isBlank()) {
            throw new MyException("The item description cannot be blank.");
        }
        if (factoryId <= 0) {
            throw new MyException("Invalid factory ID: must be greater than 0.");
        }
    }
}
