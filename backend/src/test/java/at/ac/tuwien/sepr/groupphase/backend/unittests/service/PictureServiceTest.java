package at.ac.tuwien.sepr.groupphase.backend.unittests.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PictureDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Picture;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.PictureRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.PictureService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class PictureServiceTest {

    @Autowired
    protected PictureService service;

    @Autowired
    protected PictureRepository repository;

    final private Path picturePath = Path.of((new File("")).getAbsolutePath() + "/src/test/resources/pictures/test.png");
    private byte[] pictureData;

    @BeforeEach
    void setup() throws Exception {
        pictureData = Files.readAllBytes(picturePath);
    }

    @AfterEach
    void cleanup() {
        repository.deleteAll();
    }

    @Test
    public void testCreatePicture_WithValidData_Returns() {
        Picture p1 = this.service.createPicture(pictureData, "This is a test image");

        assertNotNull(p1);
        assertAll(
            () -> assertArrayEquals(pictureData, p1.getData()),
            () -> assertEquals("This is a test image", p1.getDescription())
        );

        Picture p2 = this.service.createPicture(pictureData);

        assertNotNull(p2);
        assertAll(
            () -> assertArrayEquals(pictureData, p2.getData()),
            () -> assertNull(p2.getDescription())
        );
    }

    @Test
    public void testCreatePicture_WithNoData_ThrowsIllegalArgument() {
        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () -> this.service.createPicture(null, null)),
            () -> assertThrows(IllegalArgumentException.class, () -> this.service.createPicture(null, "This is a description")),
            () -> assertThrows(IllegalArgumentException.class, () -> this.service.createPicture(null))
        );
    }

    @Test
    public void testGetPicture_WithValidId_Returns() {
        Long picId = this.service.createPicture(pictureData, "This is a description").getId();

        Picture p = this.service.getById(picId);

        assertNotNull(p);
        assertAll(
            () -> assertEquals(picId, p.getId()),
            () -> assertArrayEquals(pictureData, p.getData()),
            () -> assertEquals("This is a description", p.getDescription())
        );
    }

    @Test
    public void testGetPicture_AsDto_WithValidId_Returns() {
        Long picId = this.service.createPicture(pictureData, "This is a description").getId();

        PictureDto p = this.service.getByIdAsDto(picId);

        assertNotNull(p);
        assertAll(
            () -> assertEquals(picId, p.getId()),
            () -> assertArrayEquals(pictureData, p.getData()),
            () -> assertEquals("This is a description", p.getDescription())
        );
    }

    @Test
    public void testGetPicture_WithInvalidId_ThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> this.service.getById(Long.MAX_VALUE));
    }

    @Test
    public void testGetPicture_AsDto_WithInvalidId_ThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> this.service.getByIdAsDto(Long.MAX_VALUE));
    }

}
