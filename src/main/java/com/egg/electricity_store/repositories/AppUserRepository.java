package com.egg.electricity_store.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.egg.electricity_store.entities.AppUser;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    // Busca usuarios por su correo electrónico, que en la mayoría de los casos, 
    // se utiliza como nombre de usuario.
    public Optional<AppUser> findByEmail(String email);
}
