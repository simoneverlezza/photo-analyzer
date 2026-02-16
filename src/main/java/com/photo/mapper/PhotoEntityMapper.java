package com.photo.mapper;

import com.photo.AppConfig;
import com.photo.model.Photo;
import com.photo.model.PhotoUploadForm;
import com.photo.utils.FileUtils;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;

import java.time.LocalDateTime;

public class PhotoEntityMapper {
    @Inject
    static AppConfig config;

    public static Photo toPhotoEntity(PhotoUploadForm uploadedPhoto) {
        Photo photo = new Photo();

        photo.setName(uploadedPhoto.getName());
        photo.setSizeMB(FileUtils.bytesToMB(Long.parseLong(uploadedPhoto.getSize())));
        photo.setChecksum(uploadedPhoto.getChecksum());
        photo.setCreatedAt(uploadedPhoto.getCreatedAt());
        photo.setUploadedAt(String.valueOf(LocalDateTime.now()));
        photo.setStoragePath(config.photosSystemDir());

        Log.infof("Created photo: %s", photo.toString());

        return photo;
    }
}
