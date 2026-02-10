package com.photo;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "app")
public interface AppConfig {
    @WithDefault("/uploads/photos")
    String downloadDir();
    @WithDefault("/images")
    String photoTestDir();
    @WithDefault("/data/photos")
    String photosSystemDir();
    @WithDefault("com.photo")
    String baseProjectPackage();
}
