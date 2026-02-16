package com.photo.mapper;

import com.photo.AppConfig;
import com.photo.model.MetadataSelection;
import com.photo.model.Photo;
import com.photo.utils.FileUtils;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Map;

public class PhotoEntityMapper {

    @Inject
    static AppConfig config;

    public static Photo toPhotoEntity(Map<String, String> metadata, String checksum, File file) {
        Photo photo = new Photo();

        photo.setName(metadata.get(file.getName()));
        photo.setSizeMB(FileUtils.bytesToMB(file.length()));
        photo.setChecksum(checksum);

        String creationDate = metadata.get(MetadataSelection.CREATION_DATE.getValue());

        Log.infof("Creation date: ", creationDate);

        if(creationDate != null) {
            photo.setCreatedAt(LocalDateTime.parse(creationDate));
        } else {
            photo.setCreatedAt(LocalDateTime.now());
        }

        photo.setMimeType(metadata.get(MetadataSelection.DETECTED_MIME_TYPE.getValue()));
        photo.setStoragePath(config.photosSystemDir());

        Log.infof("Created photo: %s", photo.toString());

        return photo;
    }
}
