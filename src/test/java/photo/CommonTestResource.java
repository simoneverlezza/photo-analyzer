package photo;

import com.photo.AppConfig;
import com.sv.filter.StackTraceFilter;
import jakarta.inject.Inject;

import java.io.File;

public class CommonTestResource {

    @Inject
    AppConfig config;

    protected static final String LOVELESS_JPG = "loveless.JPG";
    protected static final String SCREENSHOT_PNG = "screenshot.png";

    protected File loadImage(String name) {
        try {
            var url = getClass().getResource("/images/" + name);
            if (url == null) {
                throw new RuntimeException("Resource not found: " + name);
            }

            return new File(url.toURI());
        } catch (Exception e) {
            throw new RuntimeException(StackTraceFilter
                    .filterStackTrace(config.baseProjectPackage(), e));
        }
    }
}
