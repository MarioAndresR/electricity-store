package com.egg.electricity_store.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.egg.electricity_store.entities.Factory;
import com.egg.electricity_store.exceptions.MyException;
import com.egg.electricity_store.repositories.FactoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
// This annotation automatically generates a constructor with required fields (those marked as final)
// and Spring will inject the dependencies without needing to explicitly write the constructor.
public class FactoryService {
    // @Autowired -> Spring Doesn’t Recommend @Autowired Anymore, better with constructor injection
    private final FactoryRepository factoryRepository;
    
    @Transactional
    public Factory create(String factoryName) throws MyException {
        validateName(factoryName);
        Factory newFactory = new Factory();
        newFactory.setFactoryName(factoryName);
        return factoryRepository.save(newFactory);
    }

    @Transactional(readOnly = true)
    public List<Factory> getAll(){
        return factoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Factory getById(Long factoryId) throws MyException {
        return factoryRepository.findById(factoryId)
            .orElseThrow(() -> new MyException("Factory not found with ID: " + factoryId));
    }

    @Transactional
    public Factory update(Long factoryId, String factoryName) throws MyException {
        validateId(factoryId);
        validateName(factoryName);

        Factory factory = factoryRepository.findById(factoryId)
            .orElseThrow(() -> new MyException("Factory not found with ID: " + factoryId));          
            factory.setFactoryName(factoryName);

            return factory; // No need to call save(), Hibernate auto-manages changes
    }

    @Transactional
    public void deleteById(Long factoryId) throws MyException {
        validateId(factoryId);
        
        if (!factoryRepository.existsById(factoryId)) {
            throw new MyException("Factory not found with ID: " + factoryId);
        }

        factoryRepository.deleteById(factoryId);
    }

    private void validateId(Long id) throws MyException {
        if (id == null || id <= 0) {
            throw new MyException("Invalid ID: must be greater than 0.");
        }
    }

    private void validateName(String factoryName) throws MyException {
        if (factoryName == null || factoryName.trim().isEmpty()) {
            throw new MyException("The name of the factory is required.");
        }
    }

}
