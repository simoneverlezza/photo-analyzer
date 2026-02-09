package photo;

import com.photo.utils.MetadataUtils;
import io.quarkus.logging.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;


public class MetadataTest extends CommonTestResource {

    @Test
    void logAllMetadata() {
        try {
            File photo = loadImage(SCREENSHOT_PNG);
            if(photo != null) {
                MetadataUtils.extractMetadata(photo);
            } else {
                Log.infof("Photo is null");
            }
        } catch (Exception e) {
            Assertions.fail("Error occurred");
        }

    }
}
