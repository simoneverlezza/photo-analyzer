package com.photo.utils;

import com.photo.model.PhotoUploadForm;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import org.apache.tika.Tika;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class MetadataUtils {

    public static String extractCreatedAt(PhotoUploadForm uploadedPhoto) {
        try {
            return setCreatedAtDate(uploadedPhoto);
        } catch (Exception e) {
            Log.infof("Error extracting metadata from %s ... setting it to null value. Error: %s",
                    uploadedPhoto.getName(), e.getMessage());
            return "";
        }
    }

    private static String setCreatedAtDate(PhotoUploadForm uploadedPhoto) {

        try (FileInputStream fis = new FileInputStream(uploadedPhoto.getFile())) {

            Optional<Instant> creationDate = extractCreationDate(fis);
            String parsedCreationDate = "";

            if(creationDate.isPresent()) {
                parsedCreationDate = String.valueOf(LocalDateTime.parse(creationDate.get().toString()));
            }

            uploadedPhoto.setCreatedAt(parsedCreationDate);

            if(parsedCreationDate != null && !parsedCreationDate.isEmpty()) {
                Log.infof("Creation Date found and added: %s", parsedCreationDate);
                return parsedCreationDate;
            } else {
                return "";
            }

        } catch (FileNotFoundException e) {
            Log.errorf("File not found: %s", uploadedPhoto.getFile().getAbsolutePath());
            throw new RuntimeException(e);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Optional<Instant> extractCreationDate(InputStream inputStream) {
        try {
            org.apache.tika.metadata.Metadata tikaMetadata = new org.apache.tika.metadata.Metadata();
            AutoDetectParser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler();
            ParseContext context = new ParseContext();

            parser.parse(inputStream, handler, tikaMetadata, context);

            String[] priorityFields = {
                    "exif:DateTimeOriginal",
                    "exif:CreateDate",
                    "xmp:CreateDate",
                    "photoshop:DateCreated",
                    "Iptc.DateCreated"
            };

            for (String field : priorityFields) {
                String value = tikaMetadata.get(field);
                if (value != null && !value.isBlank()) {
                    Optional<Instant> parsed = parseToInstant(value.trim());
                    if (parsed.isPresent()) {
                        return parsed;
                    } else {
                        Log.infof("Tried to extract creation date from %s but nothing found", field);
                    }
                }
            }

        } catch (Exception e) {
            Log.errorf("Error extracting creation date");
        }

        return Optional.empty();
    }

    private static Optional<Instant> parseToInstant(String value) {
        try {
            // ISO 8601 (XMP)
            try {
                return Optional.of(Instant.parse(value));
            } catch (DateTimeParseException ignored) {
                Log.debugf("No ISO 8601 - %s", value);
            }

            // EXIF format: 2023:10:12 14:32:11
            DateTimeFormatter exifFormatter =
                    DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");

            try {
                LocalDateTime ldt = LocalDateTime.parse(value, exifFormatter);
                return Optional.of(ldt.atZone(ZoneId.systemDefault()).toInstant());
            } catch (DateTimeParseException ignored) {
                Log.debugf("No EXIF - %s", value);
            }

            // ISO no timezone
            try {
                LocalDateTime ldt = LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                return Optional.of(ldt.atZone(ZoneId.systemDefault()).toInstant());
            } catch (DateTimeParseException ignored) {
                Log.debugf("No ISO-No-Timezone - %s", value);
            }

        } catch (Exception ignored) {}

        return Optional.empty();
    }
}

