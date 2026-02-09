package com.photo.utils;

import com.drew.metadata.Metadata;
import io.quarkus.logging.Log;

public class MetadataUtils {

    public static void logAllMetadata(Metadata metadata) {
        metadata.getDirectories().forEach(directory ->
                directory.getTags().forEach(tag ->
                                Log.infof("[%s] %s - %s",
                                        tag.getTagName(),
                                        tag.getTagName(),
                                        tag.getDescription())));
    }
}
