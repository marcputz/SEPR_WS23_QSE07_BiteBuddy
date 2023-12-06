package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.LoginDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Test
    public void testUpdateAfterLogin_withValidData_isOk() throws Exception {
        // Login to get the token
        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);

        MvcResult loginResult = this.mockMvc.perform(post("/api/v1/authentication/login")
                .content(new ObjectMapper().writeValueAsString(LoginDto.LoginDtobuilder.anLoginDto()
                    .withEmail(TESTUSER.getEmail())
                    .withPassword("password")
                    .build()))
                .headers(loginHeaders))
            .andExpect(status().isOk())
            .andReturn();

        String authToken = loginResult.getResponse().getContentAsString();
        assertNotNull(authToken);

        // Prepare update data
        UserUpdateDto updateDto = UserUpdateDto.UserUpdateDtoBuilder.anUserUpdateDto()
            .withEmail("newemail@authEndpoint.at")
            .withName("newNickname")
            .withPassword("newPassword")
            .build();

        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        updateHeaders.setContentType(MediaType.APPLICATION_JSON);
        updateHeaders.set("Authorization", authToken);

        // Perform update request
        MvcResult updateResult = this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/authentication/settings")
                .content(new ObjectMapper().writeValueAsString(updateDto))
                .headers(updateHeaders))
            .andExpect(status().isOk())
            .andReturn();

        // Verify the response
        MockHttpServletResponse updateResponse = updateResult.getResponse();
        assertEquals(HttpStatus.OK.value(), updateResponse.getStatus());
        //TODO Additional assertions to verify the updated user data
    }

}
