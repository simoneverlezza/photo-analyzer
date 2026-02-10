package com.photo.response;

import com.photo.model.Photo;
import lombok.*;

import java.io.File;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PhotoResponse {

    List<File> files;
    List<Photo> metadata;
}
