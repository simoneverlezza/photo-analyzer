package com.photo.utils;

import com.photo.model.ArchiveFormats;
import com.photo.model.ExtractedFile;
import com.photo.model.PhotoUploadForm;
import io.quarkus.logging.Log;
import org.apache.tika.Tika;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileUtils {

    public static List<PhotoUploadForm> getRawData(List<FileUpload> files, Tika tika, String tempPath, String sourceType) {
        List<PhotoUploadForm> uploadedPhotos = new ArrayList<>();

        for(FileUpload file : files) {

            File uploadedFile = file.uploadedFile().toFile();
            String fileName = file.fileName();
            String mimeType = file.contentType();
            String size = String.valueOf(file.size());

            Log.infof("Extracted %s", uploadedFile.toString());

            try {

                Log.infof("Detected mime type: %s", mimeType);

                // Se il file è un archivio
                if (Arrays.stream(ArchiveFormats.values()).anyMatch(archiveType -> archiveType.getMimeType().equals(mimeType))) {

                    Log.infof("File is an archive");

                    List<ExtractedFile> extractedFiles = ArchiveExtractor.extract(file.filePath(), Paths.get(tempPath));
                    extractedFiles.stream().forEach(extractedFile -> {

                        PhotoUploadForm rawData = PhotoUtils.uploadedPhotoCreator(
                                extractedFile.getFile(),
                                sourceType,
                                extractedFile.getFileName(),
                                extractedFile.getMimeType(),
                                extractedFile.getSize(),
                                FileUtils.calculateChecksum(extractedFile.getFile()),
                                null,
                                null);

                        Log.infof("Extracted raw data: %s", rawData.toString());

                        uploadedPhotos.add(rawData);
                    });
                }

                PhotoUploadForm rawData = PhotoUtils.uploadedPhotoCreator(
                        uploadedFile, sourceType, fileName, mimeType, size, FileUtils.calculateChecksum(uploadedFile), null, null);

                Log.infof("Extracted raw data: %s", rawData.toString());

                uploadedPhotos.add(rawData);

            } catch (Exception e) {
                Log.errorf("Error analyzing uploaded file: %s - %s", uploadedFile.getName(), e.getMessage());
                throw new RuntimeException(e);
            }
        }

        Log.infof("%s files extracted", uploadedPhotos.size());

        return uploadedPhotos;
    }

    public static String bytesToMB(long bytes) {
        double mb = (double) bytes / (1024 * 1024);
        return String.format("%.2f", mb);
    }

    public static String calculateChecksum(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] byteArray = new byte[1024];
                int bytesCount;
                while ((bytesCount = fis.read(byteArray)) != -1) {
                    digest.update(byteArray, 0, bytesCount);
                }
            }

            byte[] bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (Exception e) {
            Log.infof("Erroe creating checksum for file: %s - %s", file.getName(), e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
