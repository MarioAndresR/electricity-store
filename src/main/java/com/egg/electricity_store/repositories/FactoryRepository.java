package com.egg.electricity_store.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.egg.electricity_store.entities.Factory;

import java.util.List;


@Repository
public interface FactoryRepository extends JpaRepository<Factory, Long> {
    
    public List<Factory> findByFactoryName(String factoryName);
}
