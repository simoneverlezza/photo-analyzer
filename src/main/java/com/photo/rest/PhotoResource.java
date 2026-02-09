package com.photo.rest;

import com.photo.AppConfig;
import com.photo.model.PhotoUploadForm;
import com.photo.service.PhotoService;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;

@Path("/photos")
public class PhotoResource {

    @Inject
    AppConfig config;
    @Inject
    PhotoService photoService;

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upload(@RestForm("file") FileUpload file, @RestForm("sourceType") String sourceType) {

        PhotoUploadForm uploadedPhoto = null;
        String tempPath = "";

        try {
            uploadedPhoto = new PhotoUploadForm(file.uploadedFile().toFile(), sourceType);
            tempPath = config.downloadDir();
        } catch (Exception e) {
            Log.infof("Error extracting uploaded file: %s - %s", file, e.getMessage());
            throw new RuntimeException(e);
        }

        photoService.analyzePhoto(uploadedPhoto, tempPath);

        return Response.ok().build();
    }
}