package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PictureDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.authentication.LoginDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Picture;
import at.ac.tuwien.sepr.groupphase.backend.repository.PictureRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class PictureEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    @Autowired
    protected PictureRepository repository;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected AuthenticationService authService;

    private ApplicationUser user;

    final private Path picturePath = Path.of((new File("")).getAbsolutePath() + "/src/test/resources/pictures/test.png");
    private byte[] pictureData;

    private Picture testPicture;

    @BeforeEach
    void setup() throws Exception {
        pictureData = Files.readAllBytes(picturePath);

        Picture p = new Picture()
            .setData(pictureData)
            .setDescription("Test Description");

        this.testPicture = this.repository.save(p);

        ApplicationUser user = new ApplicationUser();
        user.setId(1L);
        user.setNickname("testuser");
        user.setEmail("test@test");
        user.setPasswordEncoded(PasswordEncoder.encode("password", "test@test"));

        this.user = this.userRepository.save(user);
    }

    @AfterEach
    void cleanup() {
        repository.deleteAll();
        userRepository.deleteAll();
    }

    public String authenticate() throws Exception {
        LoginDto dto = new LoginDto();
        dto.setEmail(this.user.getEmail());
        dto.setPassword("password");
        return this.authService.loginUser(dto);
    }

    @Test
    void testSetPicture_LoggedIn_WithValidData_IsCreatedAndValidDto() throws Exception {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.set("Authorization", authenticate());

        MvcResult mvcResult = this.mockMvc.perform(put("/api/v1/picture")
                .content(this.objectMapper.writeValueAsString(
                    new PictureDto()
                        .setData(pictureData)
                        .setDescription("Image Description")))
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.CREATED.value(), response.getStatus());

        PictureDto responseDto = objectMapper.readerFor(PictureDto.class).readValue(response.getContentAsByteArray());

        assertAll(
            () -> assertArrayEquals(pictureData, responseDto.getData()),
            () -> assertEquals("Image Description", responseDto.getDescription()),
            () -> assertTrue(responseDto.getId() > 0)
        );
    }

    @Test
    void testSetPicture_NotLoggedIn_IsUnauthorized() throws Exception {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = this.mockMvc.perform(put("/api/v1/picture")
                .content(this.objectMapper.writeValueAsString(
                    new PictureDto()
                        .setData(pictureData)
                        .setDescription("Image Description")))
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }

    @Test
    void testSetPicture_LoggedIn_WithInvalidData_IsBadRequest() throws Exception {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.set("Authorization", authenticate());

        MvcResult mvcResult = this.mockMvc.perform(put("/api/v1/picture")
                .content(this.objectMapper.writeValueAsString(
                    new PictureDto()
                        .setData(null)
                        .setDescription("Image Description")))
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    }

    @Test
    void testSetPicture_LoggedIn_WithNoData_IsBadRequest() throws Exception {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.set("Authorization", authenticate());

        MvcResult mvcResult = this.mockMvc.perform(put("/api/v1/picture")
                .content("")
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    }

    @Test
    void testGetPicture_WithValidId_IsOk() throws Exception {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = this.mockMvc.perform(get("/api/v1/picture?id=" + testPicture.getId())
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        PictureDto responseDto = objectMapper.readerFor(PictureDto.class).readValue(response.getContentAsByteArray());

        assertAll(
            () -> assertEquals(testPicture.getId(), responseDto.getId()),
            () -> assertArrayEquals(testPicture.getData(), responseDto.getData()),
            () -> assertEquals(testPicture.getDescription(), responseDto.getDescription())
        );
    }

    @Test
    void testGetPicture_WithInvalidId_IsNotFound() throws Exception {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = this.mockMvc.perform(get("/api/v1/picture?id=" + Long.MAX_VALUE)
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
    }

    @Test
    void testGetPicture_WithNoId_IsBadRequest() throws Exception {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = this.mockMvc.perform(get("/api/v1/picture")
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    }
}
