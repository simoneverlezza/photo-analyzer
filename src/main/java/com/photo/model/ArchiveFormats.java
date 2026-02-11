package com.photo.model;

public enum ArchiveFormats {
    ZIP("application/zip"),
    RAR("application/vnd.rar"),
    TAR("application/x-tar"),
    GZ("application/gzip"),
    _7ZIP("application/x-7z-compressed");

    private final String mimeType;

    ArchiveFormats(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }
}
