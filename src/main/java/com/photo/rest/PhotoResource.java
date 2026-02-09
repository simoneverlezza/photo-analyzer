package com.photo.rest;

import com.photo.AppConfig;
import com.photo.model.PhotoUploadForm;
import com.photo.service.PhotoService;
import com.photo.utils.FileUtils;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;


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
        File uploadedFile = file.uploadedFile().toFile();

        try {
            uploadedPhoto = new PhotoUploadForm(uploadedFile, sourceType, FileUtils.calculateChecksum(uploadedFile));
            tempPath = config.downloadDir();
        } catch (Exception e) {
            Log.infof("Error extracting uploaded file: %s - %s", file, e.getMessage());
            throw new RuntimeException(e);
        }

        String message = photoService.analyzeAndPersistPhoto(uploadedPhoto, tempPath);

        return Response.ok().entity(message).build();
    }
}