package com.photo.mapper;

import com.photo.model.Photo;

public class PhotoUpdater {

    public static void updatePhoto(Photo oldPhoto, Photo newPhoto) {
        oldPhoto.setName(newPhoto.getName());
    }
}
