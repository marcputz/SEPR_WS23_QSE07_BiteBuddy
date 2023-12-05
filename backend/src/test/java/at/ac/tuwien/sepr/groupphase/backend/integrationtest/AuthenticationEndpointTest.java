package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.LoginDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.SimpleMessageDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.MessageMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Message;
import at.ac.tuwien.sepr.groupphase.backend.repository.MessageRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.annotation.AfterTestClass;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class AuthenticationEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private ApplicationUser TESTUSER = new ApplicationUser()
        .setId(-100L)
        .setNickname("authEndpointTest")
        .setEmail("test@authEndpoint.at")
        .setPasswordEncoded(PasswordEncoder.encode("password", "test@authEndpoint.at"));

    private long testUserId;

    @BeforeEach
    public void beforeEach() {
        testUserId = userRepository.save(TESTUSER).getId();
    }

    @AfterEach
    public void afterEach() {
        userRepository.deleteById(testUserId);
    }

    @Test
    public void testLoginApi_withValidData_isOk() throws Exception {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/authentication/login")
                .content((new ObjectMapper()).writeValueAsString(LoginDto.LoginDtobuilder.anLoginDto()
                    .withEmail(TESTUSER.getEmail())
                    .withPassword("password")
                    .build()))
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        String authToken = response.getContentAsString();
        assertNotNull(authToken);
        assertEquals("Token ", authToken.substring(0, 6));
    }

    @Test
    public void testLoginApi_withInvalidEmail_isAuthenticationError() throws Exception {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/authentication/login")
                .content((new ObjectMapper()).writeValueAsString(LoginDto.LoginDtobuilder.anLoginDto()
                    .withEmail("doesNotExist@shouldFail.net")
                    .withPassword("password")
                    .build()))
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
    }

    @Test
    public void testLoginApi_withInvalidPassword_isAuthenticationError() throws Exception {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/authentication/login")
                .content((new ObjectMapper()).writeValueAsString(LoginDto.LoginDtobuilder.anLoginDto()
                    .withEmail(TESTUSER.getEmail())
                    .withPassword("thisIsAWrongPassword")
                    .build()))
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
    }

    @Test
    public void testLogoutApi_withValidToken_isOk() throws Exception {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/authentication/login")
                .content((new ObjectMapper()).writeValueAsString(LoginDto.LoginDtobuilder.anLoginDto()
                    .withEmail(TESTUSER.getEmail())
                    .withPassword("password")
                    .build()))
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        String authToken = response.getContentAsString();
        requestHeaders.set("authorization", authToken);

        mvcResult = this.mockMvc.perform(post("/api/v1/authentication/logout")
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
    }

    @Test
    public void testLogoutApi_withInvalidToken_isBadRequest() throws Exception {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.set("authorization", "Token nonexistingauthtoken");

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/authentication/logout")
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
    }

}
