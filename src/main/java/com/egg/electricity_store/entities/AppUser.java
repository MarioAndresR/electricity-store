package com.egg.electricity_store.entities;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.egg.electricity_store.enumerations.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
// Override equals() and hashCode() properly
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Only the fields marked (.Include) are used
@Builder // -> Implements Builder design pattern for object creation
@Entity
@Table(name = "app_user") // -> Explicitly set the table name to avoid conflicts
public class AppUser implements UserDetails {
// Called "AppUser" because "User" is a Reserved Name in Spring Security
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL AUTO_INCREMENT
    private Long appUserId;

    @Email(message ="Please provide a valid email address") //Hibernate's built-in email validation
    @Column(nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String email;
    
    @Column(nullable = false)
    @EqualsAndHashCode.Include
    private String name;
    
    @Column(nullable = false)
    @EqualsAndHashCode.Include
    private String lastName;

    @Column(nullable = false)
    private String password;

    /*
     * ==================================================================================
     *         📌 Why not using columnDefinition = "VARCHAR(10) DEFAULT 'USER'"
     * ==================================================================================
     * In this case, we do not use columnDefinition in @Column to set a default value via 
     * SQL DDL because Hibernate ignores DEFAULT values in DDL most of the time, instead
     * it sends all values explicitly.
     * 
     * This sets the default value at the database level, but NOT in your Java object 
     * when it is created in code. If you don't explicitly set role in Java, it will be 
     * null and when Hibernate tries to persist this entity, you'll get a 
     * ConstraintViolationException (if it is required). Even though the DB column has a 
     * default, Hibernate doesn’t trigger that default when inserting a row — because 
     * it is using an INSERT that includes the role=null.
     * 
     * That is why the best practice would be to use @Builder.Default, when using @Builder 
     * and want a default value in the builder, plus private Role role = Role.USER; 
     * to cover constructor-based creation and ORM instantiation.
     * 
     * This ensures:
     *  - Hibernate will never insert null
     *  - Java has a default
     *  - Builder respects the default
     * 
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default // Set the default value if not explicitly set during building (Service Class).
    private Role role = Role.USER; // Initializing expression (default value)

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;

    /*
     * ========================================================================
     *                   Implementation of UserDetails methods
     * ========================================================================
     * In order to match "ROLE_USER" for Spring Security, you must manually 
     * prefix it ("ROLE_") when building the GrantedAuthority.
     * 
     */ 
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()) );
    }

    @Override
    public String getUsername() {
        return email; // Using email as the username
    }

    @Override
    public String getPassword() {
        return password;  // Spring Security retrieves this for authentication
    }
}
