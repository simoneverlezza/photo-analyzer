package com.photo.rest;

import com.photo.AppConfig;
import com.photo.model.ArchiveFormats;
import com.photo.model.Outcome;
import com.photo.model.Photo;
import com.photo.model.PhotoUploadForm;
import com.photo.response.PhotoResponse;
import com.photo.service.PhotoService;
import com.photo.utils.ArchiveExtractor;
import com.photo.utils.FileUtils;
import com.photo.utils.PhotoUtils;
import com.sv.filter.StackTraceFilter;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.tika.Tika;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


@Path("/photos")
public class PhotoResource {
    @Inject
    AppConfig config;
    @Inject
    PhotoService photoService;
    Tika tika = new Tika();

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upload(@RestForm("file") List<FileUpload> files, @RestForm("sourceType") String sourceType) {

        Log.infof("Received upload photo request");

        String tempPath = config.downloadDir();

        List<PhotoUploadForm> uploadedPhotos = FileUtils.getRawData(files, tika, tempPath, sourceType);

        Map<Outcome, String> outcomes = photoService.analyzeAndPersistPhotos(uploadedPhotos, tempPath);

        List<String> notPersistedPhotos = outcomes.entrySet().stream()
                .filter(entry -> entry.getKey().equals(Outcome.FAILURE))
                .map(Map.Entry::getValue)
                .toList();

        if(notPersistedPhotos.size() > 0) {
            return Response.ok().entity("Le seguenti foto non sono state salvate: " +
                    Arrays.toString(notPersistedPhotos.toArray())).build();
        }

        return Response.ok().entity("Tutte le foto sono state salvate").build();
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