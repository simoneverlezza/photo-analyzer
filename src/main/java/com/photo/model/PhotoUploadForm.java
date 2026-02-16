package com.photo.model;

import com.drew.lang.annotations.NotNull;
import lombok.*;
import org.jboss.resteasy.reactive.RestForm;

import java.io.File;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PhotoUploadForm {
    @RestForm("file")
    @NotNull
    private File file;
    @RestForm
    @NotNull
    private String sourceType;
    private String mimeType;
    String checksum;
}
