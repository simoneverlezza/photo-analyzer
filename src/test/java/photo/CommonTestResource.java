package photo;

import java.io.File;

public class CommonTestResource {

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
            throw new RuntimeException(e);
        }
    }
}
