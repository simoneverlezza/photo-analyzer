package com.photo.utils;

import com.photo.AppConfig;
import com.sv.filter.StackTraceFilter;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import org.apache.commons.compress.archivers.*;
import org.apache.commons.compress.compressors.*;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class ArchiveExtractor {

    public static List<File> extract(Path archive, Path destination) throws Exception {

        List<File> extractedFiles = new ArrayList<>();

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

                            extractedFiles.add(extractedFile);

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

