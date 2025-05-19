package com.egg.electricity_store.entities;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL AUTO_INCREMENT
    private Long imageId;

    @Column(nullable = false)
    private String imageName;

    @Column(nullable = false)
    private String imageMime;

    @Lob
    @Basic(fetch = FetchType.LAZY) // <- Lazy Loading
    @Column(columnDefinition = "LONGBLOB")
    private byte[] imageContent;
}
