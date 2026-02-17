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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PhotoService {
    @Inject
    PhotoRepository photoRepository;
    @Inject
    PhotoEntityMapper photoEntityMapper;
    @Inject
    AppConfig config;

    public Map<Outcome, String> analyzeAndPersistPhotos(List<PhotoUploadForm> rawPhotoData, String tempPath) {
        Log.infof("Starting photo analysis");

        Map<Photo, File> photoFiles = new HashMap<>();

        rawPhotoData.stream().forEach(uploadedPhoto -> {
            String createdAt = MetadataUtils.extractCreatedAt(uploadedPhoto);
            uploadedPhoto.setCreatedAt(createdAt);
            photoFiles.put(photoEntityMapper.toPhotoEntity(uploadedPhoto), uploadedPhoto.getFile());
        });

        Log.infof("Analysis ended, persisting photo");
        return persist(photoFiles);
    }

    private Map<Outcome, String> persist(Map<Photo, File> photoFiles) {
        Log.infof("Persisting photo");

        Map<Outcome, String> outcomes = new HashMap<>();

        photoFiles.forEach((photo, file) -> {

            Path targetPath = Paths.get(config.photosSystemDir()).toAbsolutePath().resolve(photo.getName());

            Log.infof("About to move %s to %s", file.toPath(), targetPath);

            try {
                Files.move(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                Outcome outcome = photoRepository.saveToDB(photo);
                outcomes.put(outcome, file.toString());
            } catch (IOException e) {
                Log.infof("Error moving %s to %s - %s", file.getName(), config.photosSystemDir(), e);
                outcomes.put(Outcome.FAILURE, file.toString());
            }
        });

        return outcomes;
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
