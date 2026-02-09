package com.photo.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.photo.model.Photo;
import com.photo.model.PhotoUploadForm;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.tika.Tika;

import java.io.File;
import java.io.IOException;

@ApplicationScoped
public class PhotoService {

    static final Tika tika = new Tika();

    public void analyzePhoto(PhotoUploadForm uploadedPhoto, String tempPath) {
        Metadata metadata = extractMetadata(uploadedPhoto.getFile());

    }
    private Metadata extractMetadata(File photo) {
        try {
            return ImageMetadataReader.readMetadata(photo);
        } catch (Exception e) {
            Log.infof("Error extracting metadata from %s ... setting it to null value. Error: %s",
                    photo.getName(), e.getMessage());
            return null;
        }
    }

    private void persist(Photo photo, String tempPath) {

    }
}
