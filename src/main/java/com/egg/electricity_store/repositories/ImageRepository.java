package com.egg.electricity_store.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.egg.electricity_store.entities.Image;
import java.util.List;


@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    public List<Image> findByImageName(String imageName);
}