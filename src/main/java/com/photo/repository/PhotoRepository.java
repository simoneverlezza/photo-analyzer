package com.photo.repository;

import com.photo.mapper.PhotoUpdater;
import com.photo.model.Outcome;
import com.photo.model.Photo;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class PhotoRepository implements PanacheRepository<Photo> {
    @Transactional
    public Outcome saveToDB(Photo photo) {
        try {
            persist(photo);
            return Outcome.SUCCESS;
        } catch (Exception e) {
            Log.infof("Error saving photo metadata to DB: %s", e.getMessage());
            return Outcome.FAILURE;
        }
    }

    @Transactional
    public Outcome saveBatchToDB(List<Photo> photos) {
        try {
            persist(photos);
            return Outcome.SUCCESS;
        } catch (Exception e) {
            Log.infof("Error saving photos metadata to DB: %s", e.getMessage());
            return Outcome.FAILURE;
        }
    }

    public List<Photo> getAllPhotos() {
        try {
            listAll();
        } catch (Exception e) {
            Log.infof("Error getting photos metadata from DB: %s", e.getMessage());
        }

        return null;
    }

    public Photo getPhotoById(long id) {
        try {
            return findById(id);
        } catch (Exception e) {
            Log.infof("Error getting photos metadata from DB: %s", e.getMessage());
        }

        return null;
    }

    public Outcome deletePhoto(long id) {
        try {
            deleteById(id);
            return Outcome.SUCCESS;
        } catch (Exception e) {
            Log.infof("Error deleting photo with id: %s - %s", id, e.getMessage());
        }

        return Outcome.FAILURE;
    }

    @Transactional
    public Outcome updatePhoto(Photo oldPhoto, Photo newPhoto) {
        try {
            PhotoUpdater.updatePhoto(oldPhoto, newPhoto);
            return Outcome.SUCCESS;
        } catch (Exception e) {
            Log.infof("Error updating photo with id %s - %s", oldPhoto.id, e.getMessage());
        }

        return Outcome.FAILURE;
    }

    public String findStoragePathById(Long id) {
        return getEntityManager()
                .createQuery("SELECT p.storagePath from Photo p WHERE p.id = :id", String.class)
                .setParameter("id", id)
                .getSingleResult();
    }
}
