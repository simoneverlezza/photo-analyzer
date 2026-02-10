package com.photo.rest;

import com.photo.AppConfig;
import com.photo.model.Outcome;
import com.photo.model.Photo;
import com.photo.model.PhotoUploadForm;
import com.photo.response.PhotoResponse;
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
import java.util.ArrayList;
import java.util.List;


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

        Log.infof("Received upload photo request");

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

        Log.infof("Response: %s", message);

        return Response.ok().entity(message).build();
    }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getAllPhotos() {

        Log.infof("Received get all photos request");

        List<Photo> photos = photoService.getAllPhotos();

        if (photos == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No photos found").build();
        }

        List<File> photoFiles = new ArrayList<>();

        for (Photo photo : photos) {
            photoFiles.add(new File(photo.getStoragePath()));
        }

        PhotoResponse photoResponse = new PhotoResponse(photoFiles, photos);

        return Response.ok().entity(photoResponse).build();
    }
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getPhoto(@PathParam("id") long id) {

        Log.infof("Received get photo request");

        Photo photo = photoService.findById(id);

        if (photo == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Photo not found with id %s" + id).build();
        }

        File file = new File(photo.getStoragePath());

        PhotoResponse photoResponse = new PhotoResponse(List.of(file), List.of(photo));

        return Response.ok()
                .entity(photoResponse)
                .build();
    }

    @DELETE
    @Path("/delete/{id}")
    public Response deletePhoto(@PathParam("id") long id) {

        Log.infof("Received delete photo request");

        Outcome outcome = photoService.delete(id);

        if(outcome.equals(Outcome.SUCCESS)) {
            return Response.ok().entity("Photo deleted with id " + id).build();
        }

        return Response.serverError().entity("Could not delete photo with id " + id).build();
    }

    @PUT
    @Path("/{id}")
    public Response updatePhoto(@PathParam("id") long id, Photo photo) {

        Log.infof("Received update photo request");

        Outcome outcome = photoService.updateMetadata(photo, id);

        if(outcome.equals(Outcome.SUCCESS)) {
            return Response.ok().entity("Photo metadata updated").build();
        }

        return Response.serverError().entity("Could not update photo metadata with id " + id).build();
    }
}