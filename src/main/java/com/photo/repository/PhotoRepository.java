package com.photo.repository;

import com.photo.model.Photo;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class PhotoRepository implements PanacheRepository<Photo> {

    public String saveToDB(Photo photo) {
        persist(photo);
        return photo.getName() + " saved successfully";
    }

    public List<Photo> getAllPhotos() {
        return listAll();
    }
}
