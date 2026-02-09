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
    public File file;
    @RestForm
    @NotNull
    public String sourceType;
    String checksum;
}
