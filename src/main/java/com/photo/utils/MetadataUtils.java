package com.photo.utils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.photo.model.MetadataSelection;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import org.apache.tika.Tika;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MetadataUtils {

    @Inject
    static Tika tika;

    public static Map<String, String> extractMetadata(File photo, String mimeType) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(photo);
            ExifSubIFDDirectory exifMetadata = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            return filterMetadata(metadata, exifMetadata, photo, mimeType);
        } catch (Exception e) {
            Log.infof("Error extracting metadata from %s ... setting it to null value. Error: %s",
                    photo.getName(), e.getMessage());
            return null;
        }
    }

    private static Map<String, String> filterMetadata(Metadata metadata, ExifSubIFDDirectory exifMetadata, File photo, String mimeType) {
        Map<String, String> result = new HashMap<>();

        result.put(MetadataSelection.FILE_NAME.getValue(), photo.getName());
        result.put(MetadataSelection.FILE_SIZE.getValue(), FileUtils.bytesToMB(photo.length()));
        result.put(MetadataSelection.DETECTED_MIME_TYPE.getValue(), mimeType);

        try {
            Optional<Instant> creationDate = extractCreationDate(new FileInputStream(photo));

            String parsedCreationDate = String.valueOf(LocalDateTime.parse(creationDate.get().toString()));

            if(creationDate.isPresent()) {
                result.put(MetadataSelection.CREATION_DATE.getValue(), parsedCreationDate);
            }

            Log.infof("Creation Date found and added: %s", parsedCreationDate);

        } catch (FileNotFoundException e) {
            Log.errorf("File not found: %s", photo.getAbsolutePath());
            throw new RuntimeException(e);
        }

        result.put(MetadataSelection.UPLOAD_DATE.getValue(), String.valueOf(LocalDateTime.now()));

        return result;
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

