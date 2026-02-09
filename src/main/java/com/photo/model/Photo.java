package com.photo.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "photos")
public class Photo extends PanacheEntity {

    @Id
    private long id;
    private String name;
    private String mimeType;
    private LocalDateTime createdAt;
    private String storagePath;
    private String sizeMB;
    private String checksum;

}
