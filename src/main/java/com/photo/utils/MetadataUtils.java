package com.photo.utils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.photo.model.MetadataErrors;
import com.photo.model.MetadataSelection;
import io.quarkus.logging.Log;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class MetadataUtils {

    public static Map<String, String> extractMetadata(File photo) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(photo);
            Log.infof(metadata.toString());
            ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            return filterMetadata(directory);
        } catch (Exception e) {
            Log.infof("Error extracting metadata from %s ... setting it to null value. Error: %s",
                    photo.getName(), e.getMessage());
            return null;
        }
    }

    public static Map<String, String> filterMetadata(ExifSubIFDDirectory metadata) {
        Map<String, String> result = new HashMap<>();

        metadata.getTags().forEach(tag -> Log.infof("%s - %s", tag.getTagName(), tag.getDescription()));

        if(metadata.getDateOriginal() != null) {
            result.put(MetadataSelection.CREATION_DATE.getValue(),
                    LocalDateTime.parse(metadata.getDateOriginal().toString()).toString());
        } else {
            result.put(MetadataSelection.CREATION_DATE.getValue(),
                    MetadataErrors.NO_CREATION_DATE.name());
        }

        metadata.getTags().forEach(tag -> {

            if(tag.getTagName().equals(MetadataSelection.FILE_NAME.getValue())) {
                result.put(MetadataSelection.FILE_NAME.getValue(), tag.getDescription());
            }

            if(tag.getTagName().equals(MetadataSelection.DETECTED_MIME_TYPE.getValue())) {
                result.put(MetadataSelection.DETECTED_MIME_TYPE.getValue(), tag.getDescription());
            }

            if(tag.getTagName().equals(MetadataSelection.FILE_SIZE.getValue())) {
                result.put(MetadataSelection.FILE_SIZE.getValue(), tag.getDescription());
            }

            Log.infof("New metadata property: %s - %s", tag.getTagName(), tag.getDescription());

        });

        return result;
    }
}
