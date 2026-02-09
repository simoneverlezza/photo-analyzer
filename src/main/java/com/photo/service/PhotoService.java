package com.photo.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.photo.mapper.PhotoEntityMapper;
import com.photo.model.Photo;
import com.photo.model.PhotoUploadForm;
import com.photo.repository.PhotoRepository;
import com.photo.utils.MetadataUtils;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PhotoService {
    @Inject
    PhotoRepository photoRepository;

    public String analyzeAndPersistPhoto(PhotoUploadForm uploadedPhoto, String tempPath) {
        Log.infof("Starting photo analysis");
        Map<String, String> metadata = MetadataUtils.extractMetadata(uploadedPhoto.getFile());
        Photo photo = PhotoEntityMapper.toPhotoEntity(metadata, uploadedPhoto.getChecksum());
        Log.infof("Analysis ended, persisting photo");
        return persist(photo);
    }

    private String persist(Photo photo) {
        return photoRepository.saveToDB(photo);
    }

    private List<Photo> getAllPhotos() {
        Log.infof("Getting all photos");
        return photoRepository.getAllPhotos();
    }
}
