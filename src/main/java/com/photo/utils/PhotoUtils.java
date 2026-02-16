package com.photo.utils;

import com.photo.model.PhotoUploadForm;
import io.quarkus.logging.Log;

import java.io.File;

public class PhotoUtils {

    public static PhotoUploadForm uploadedPhotoCreator(File uploadedFile, String sourceType, String checksum, String mimeType) {
        try {
            return new PhotoUploadForm(uploadedFile, sourceType, FileUtils.calculateChecksum(uploadedFile), mimeType);
        } catch (Exception e) {
            Log.infof("Error creating uploaded photo object for file: %s - %s", uploadedFile.getName(), e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
