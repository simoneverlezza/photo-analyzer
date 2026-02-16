package com.photo.model;

public enum MetadataSelection {
    FILE_NAME("File Name"),
    FILE_SIZE("File Size"),
    DETECTED_MIME_TYPE("Detected MIME Type"),
    CREATION_DATE("Creation Date"),
    UPLOAD_DATE("Upload Date");

    private final String value;

    MetadataSelection(String value) {
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}
