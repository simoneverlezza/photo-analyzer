package com.photo.utils;

import com.photo.model.ExtractedFile;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import org.apache.commons.compress.archivers.*;
import org.apache.commons.compress.compressors.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class ArchiveExtractor {

    public static List<ExtractedFile> extract(Path archive, Path destination) throws Exception {

        List<ExtractedFile> extractedFiles = new ArrayList<>();

        try (InputStream fi = Files.newInputStream(archive); BufferedInputStream bi = new BufferedInputStream(fi)) {

            InputStream input;

            try {
                input = new CompressorStreamFactory().createCompressorInputStream(bi);
            } catch (CompressorException e) {
                input = bi;
            }

            try (ArchiveInputStream archiveStream = new ArchiveStreamFactory().createArchiveInputStream(input)) {

                ArchiveEntry entry;

                while ((entry = archiveStream.getNextEntry()) != null) {

                    Path outputPath = destination.resolve(entry.getName()).normalize();

                    if (!outputPath.startsWith(destination)) {
                        throw new IOException("Bad zip entry");
                    }

                    if (entry.isDirectory()) {
                        Files.createDirectories(outputPath);
                    } else {
                        Files.createDirectories(outputPath.getParent());

                        try (OutputStream o = Files.newOutputStream(outputPath)) {

                            archiveStream.transferTo(o);

                            File extractedFile = outputPath.toFile();
                            String name = extractedFile.getName();
                            String mimeType = Files.probeContentType(extractedFile.toPath());
                            String size = String.valueOf(extractedFile.length());

                            extractedFiles.add(new ExtractedFile(extractedFile, mimeType, name, size));

                            Log.infof("Extracted file %s", extractedFile);

                        } catch (Exception e) {
                            Log.errorf("Error transferring entry %s to %s", entry.getName(), outputPath);
                            throw new RuntimeException(e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                Log.errorf("Error extracting archive %s", archive);
            }
        }

        return extractedFiles;
    }
}

