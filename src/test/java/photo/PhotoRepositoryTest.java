package photo;

import com.photo.model.Photo;
import com.photo.repository.PhotoRepository;
import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@Transactional
class PhotoRepositoryTest extends CommonTestResource {

    @Inject
    PhotoRepository photoRepository;

    @Test
    void correctStoragePathReturned() {
        Photo photo = new Photo();
        photo.setStoragePath("/tmp/test.jpg");

        Log.infof("Storage path set: %s", photo.getStoragePath());

        photoRepository.persist(photo);

        String path = photoRepository.findStoragePathById(photo.id);

        Log.infof("Storage path found: %s", path);

        assertEquals("/tmp/test.jpg", path);
    }
}
