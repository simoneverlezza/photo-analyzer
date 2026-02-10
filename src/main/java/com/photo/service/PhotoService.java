package com.photo.service;

import com.photo.AppConfig;
import com.photo.mapper.PhotoEntityMapper;
import com.photo.model.Outcome;
import com.photo.model.Photo;
import com.photo.model.PhotoUploadForm;
import com.photo.repository.PhotoRepository;
import com.photo.utils.MetadataUtils;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PhotoService {
    @Inject
    PhotoRepository photoRepository;
    @Inject
    AppConfig config;

    public String analyzeAndPersistPhoto(PhotoUploadForm uploadedPhoto, String tempPath) {
        Log.infof("Starting photo analysis");
        Map<String, String> metadata = MetadataUtils.extractMetadata(uploadedPhoto.getFile());
        Photo photo = PhotoEntityMapper.toPhotoEntity(metadata, uploadedPhoto.getChecksum());
        Log.infof("Analysis ended, persisting photo");
        return persist(photo, uploadedPhoto.getFile());
    }

    private String persist(Photo photo, File actualFile) {
        Log.infof("Persisting photo");
        try {
            Files.move(actualFile.toPath(), Paths.get(config.photosSystemDir()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            Log.infof("Error moving %s to %s - %s", actualFile.getName(), config.photosSystemDir(), e.getMessage());
            return "Error moving " + actualFile.getName() + " to storage path. Metadata won't be persisted";
        }

        Outcome outcome = photoRepository.saveToDB(photo);

        if(outcome.equals(Outcome.SUCCESS)) {
            return photo.getName() + " saved successfully";
        } else {
            return photo.getName() + " not saved";
        }
    }

    public List<Photo> getAllPhotos() {
        Log.infof("Getting all photos");
        return photoRepository.getAllPhotos();
    }

    public Photo findById(long id) {
        Log.infof("Getting photo with id %s", id);
        return photoRepository.getPhotoById(id);
    }

    public Outcome delete(long id) {
        Log.infof("Deleting photo with id %s", id);
        String storagePath = photoRepository.findStoragePathById(id);
        try {
            Files.delete(Paths.get(storagePath));
        } catch (IOException e) {
            Log.infof("Error deleting photo file from storage with id %s - %s", id, e.getMessage());
        }
        return photoRepository.deletePhoto(id);
    }

    public Outcome updateMetadata(Photo photo, long id) {
        Log.infof("Updating photo with id %s", id);
        Photo oldPhoto = photoRepository.getPhotoById(id);

        if(oldPhoto != null) {
            return photoRepository.updatePhoto(oldPhoto, photo);
        }

        return Outcome.FAILURE;
    }
}
