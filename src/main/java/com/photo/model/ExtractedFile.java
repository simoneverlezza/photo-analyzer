package com.photo.model;

import lombok.*;

import java.io.File;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ExtractedFile {
    private File file;
    private String mimeType;
}
