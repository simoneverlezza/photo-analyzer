package com.photo.mapper;

import com.photo.AppConfig;
import com.photo.model.MetadataSelection;
import com.photo.model.Photo;
import com.photo.utils.FileUtils;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.Map;

public class PhotoEntityMapper {

    @Inject
    static AppConfig config;

    public static Photo toPhotoEntity(Map<String, String> metadata, String checksum) {
        Photo photo = new Photo();

        photo.setName(metadata.get(MetadataSelection.FILE_NAME.getValue()));
        photo.setSizeMB(FileUtils.bytesToMB(Long.parseLong(metadata.get(MetadataSelection.FILE_SIZE.getValue()))));
        photo.setChecksum(checksum);
        photo.setCreatedAt(LocalDateTime.parse(metadata.get(MetadataSelection.CREATION_DATE.getValue())));
        photo.setMimeType(metadata.get(MetadataSelection.DETECTED_MIME_TYPE.getValue()));
        photo.setStoragePath(config.photosSystemDir());

        Log.infof("Created photo: %s", photo.toString());

        return photo;
    }
}
