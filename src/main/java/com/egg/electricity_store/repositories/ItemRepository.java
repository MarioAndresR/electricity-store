package com.egg.electricity_store.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.egg.electricity_store.entities.Item;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    // Find the max/last value of itemNumber
    @Query("SELECT MAX(i.itemNumber) FROM Item i")
    Optional<Integer> findMaxItemNumber();
}