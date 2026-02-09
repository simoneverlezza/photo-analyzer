package com.photo.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "photos")
public class Photo extends PanacheEntityBase {

    private UUID id;
    private String name;
    private String mimeType;
    private LocalDateTime createdAt;
    private String storagePath;
    private long sizeMB;
    private String checksum;

}
