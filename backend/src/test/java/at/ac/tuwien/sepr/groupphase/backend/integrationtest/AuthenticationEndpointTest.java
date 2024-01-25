package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.*;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan.MenuPlanDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.PasswordResetRequest;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.PasswordResetRequestRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
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

import java.time.LocalDateTime;
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
    private PasswordResetRequestRepository passwordResetRequestRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final String testUserPassword = "password";
    private final ApplicationUser testuser = new ApplicationUser()
        .setId(-100L)
        .setNickname("authEndpointTest")
        .setEmail("test@authEndpoint.at")
        .setPasswordEncoded(PasswordEncoder.encode(testUserPassword, "test@authEndpoint.at"));


    private final ApplicationUser testuser2 = new ApplicationUser()
        .setId(-101L)
        .setNickname("secondTestUser")
        .setEmail("secondTest@authEndpoint.at")
        .setPasswordEncoded(PasswordEncoder.encode("password2", "secondTest@authEndpoint.at"));

    private long testUserId;

    private long testUserId2;

    @BeforeEach
    public void beforeEach() throws UserNotFoundException, MessagingException {
        testUserId = userRepository.save(testuser).getId();
        testUserId2 = userRepository.save(testuser2).getId();
    }

    @AfterEach
    public void afterEach() {
        passwordResetRequestRepository.deleteByUser(testuser.setId(testUserId));
        userRepository.deleteAll();
    }

    @Test
    public void testLoginApi_withValidData_isOk() throws Exception {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/authentication/login")
                .content((new ObjectMapper()).writeValueAsString(LoginDto.LoginDtobuilder.anLoginDto()
                    .withEmail(testuser.getEmail())
                    .withPassword(testUserPassword)
                    .build()))
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        String authToken = response.getContentAsString();
        assertNotNull(authToken);
        assertEquals("Bearer ", authToken.substring(0, 7));
    }

    @Test
    public void testLoginApi_withInvalidEmail_isAuthenticationError() throws Exception {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/authentication/login")
                .content((new ObjectMapper()).writeValueAsString(LoginDto.LoginDtobuilder.anLoginDto()
                    .withEmail("doesNotExist@shouldFail.net")
                    .withPassword(testUserPassword)
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
                    .withEmail(testuser.getEmail())
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
                    .withEmail(testuser.getEmail())
                    .withPassword(testUserPassword)
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

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
    }

    @Test
    public void testUpdateEmailAndPassword_withValidData_isSuccessful() throws Exception {
        // Login to get the token
        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);

        MvcResult loginResult = this.mockMvc.perform(post("/api/v1/authentication/login")
                .content(new ObjectMapper().writeValueAsString(LoginDto.LoginDtobuilder.anLoginDto()
                    .withEmail(testuser.getEmail())
                    .withPassword(testUserPassword)
                    .build()))
                .headers(loginHeaders))
            .andExpect(status().isOk())
            .andReturn();

        String authToken = loginResult.getResponse().getContentAsString();
        assertNotNull(authToken);

        // Prepare update data
        UserUpdateEmailAndPasswordDto updateDto = UserUpdateEmailAndPasswordDto.UserUpdateDtoBuilder.anUserUpdateDto()
            .withEmail("newemail@authEndpoint.at")
            .withCurrentPassword(testUserPassword)
            .withNewPassword("newPassword")
            .build();

        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        updateHeaders.setContentType(MediaType.APPLICATION_JSON);
        updateHeaders.set("Authorization", authToken);

        // Perform update request
        MvcResult updateResult = this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/authentication/settings/authentication")
                .content(new ObjectMapper().writeValueAsString(updateDto))
                .headers(updateHeaders))
            .andExpect(status().isOk())
            .andReturn();

        // Verify the response
        MockHttpServletResponse updateResponse = updateResult.getResponse();
        assertEquals(HttpStatus.OK.value(), updateResponse.getStatus());
        UserSettingsDto updatedUserSettings = objectMapper.readValue(updateResponse.getContentAsString(), UserSettingsDto.class);
        assertEquals("newemail@authEndpoint.at", updatedUserSettings.getEmail());
    }

    @Test
    public void testUpdateEmailAndPasswordAndReLogin_withValidCredentials_isSuccessful() throws Exception {
        // Login to get the token
        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);

        MvcResult loginResult = this.mockMvc.perform(post("/api/v1/authentication/login")
                .content(new ObjectMapper().writeValueAsString(LoginDto.LoginDtobuilder.anLoginDto()
                    .withEmail(testuser.getEmail())
                    .withPassword(testUserPassword)
                    .build()))
                .headers(loginHeaders))
            .andExpect(status().isOk())
            .andReturn();

        String authToken = loginResult.getResponse().getContentAsString();
        assertNotNull(authToken);

        String newEmail = "newemail@authEndpoint.at";
        String newPassword = "newPassword";
        // Prepare update data
        UserUpdateEmailAndPasswordDto updateDto = UserUpdateEmailAndPasswordDto.UserUpdateDtoBuilder.anUserUpdateDto()
            .withEmail(newEmail)
            .withCurrentPassword(testUserPassword)
            .withNewPassword(newPassword)
            .build();

        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        updateHeaders.setContentType(MediaType.APPLICATION_JSON);
        updateHeaders.set("Authorization", authToken);

        // Perform update request
        MvcResult updateResult = this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/authentication/settings/authentication")
                .content(new ObjectMapper().writeValueAsString(updateDto))
                .headers(updateHeaders))
            .andExpect(status().isOk())
            .andReturn();

        // Verify the response
        MockHttpServletResponse updateResponse = updateResult.getResponse();
        assertEquals(HttpStatus.OK.value(), updateResponse.getStatus());

        // Logout
        HttpHeaders logoutHeaders = new HttpHeaders();
        logoutHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        logoutHeaders.setContentType(MediaType.APPLICATION_JSON);
        logoutHeaders.set("Authorization", authToken);

        MvcResult logoutResult = this.mockMvc.perform(post("/api/v1/authentication/logout")
                .headers(logoutHeaders))
            .andExpect(status().isOk())
            .andReturn();
        assertEquals(HttpStatus.OK.value(), logoutResult.getResponse().getStatus());

        // Perform another login with the new data
        MvcResult secondLoginResult = this.mockMvc.perform(post("/api/v1/authentication/login")
                .content(new ObjectMapper().writeValueAsString(LoginDto.LoginDtobuilder.anLoginDto()
                    .withEmail(newEmail) // Use the new email for the second login
                    .withPassword(newPassword) // Use the new password for the second login
                    .build()))
                .headers(loginHeaders))
            .andExpect(status().isOk())
            .andReturn();

        String secondAuthToken = secondLoginResult.getResponse().getContentAsString();
        assertNotNull(secondAuthToken);
    }

    @Test
    public void testUpdateEmailAndPassword_withEmailInUse_isConflict() throws Exception {
        // Login to get the token for TESTUSER
        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);

        MvcResult loginResult = this.mockMvc.perform(post("/api/v1/authentication/login")
                .content(new ObjectMapper().writeValueAsString(LoginDto.LoginDtobuilder.anLoginDto()
                    .withEmail(testuser.getEmail())
                    .withPassword(testUserPassword)
                    .build()))
                .headers(loginHeaders))
            .andExpect(status().isOk())
            .andReturn();
        String authToken = loginResult.getResponse().getContentAsString();
        assertNotNull(authToken);
        UserUpdateEmailAndPasswordDto updateDto = UserUpdateEmailAndPasswordDto.UserUpdateDtoBuilder.anUserUpdateDto()
            .withEmail(testuser2.getEmail()) // Email already in use by secondTestUser
            .withCurrentPassword(testUserPassword)
            .withNewPassword("newPassword")
            .build();
        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        updateHeaders.setContentType(MediaType.APPLICATION_JSON);
        updateHeaders.set("Authorization", authToken);

        // Perform update request
        MvcResult updateResult = this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/authentication/settings/authentication")
                .content(new ObjectMapper().writeValueAsString(updateDto))
                .headers(updateHeaders))
            .andReturn();

        // Verify the response
        MockHttpServletResponse updateResponse = updateResult.getResponse();
        assertEquals(HttpStatus.CONFLICT.value(), updateResponse.getStatus());
    }

    @Test
    public void testRegister_withValidData_isOk() throws Exception {
        // Login to get the token
        HttpHeaders registerHeaders = new HttpHeaders();
        registerHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        registerHeaders.setContentType(MediaType.APPLICATION_JSON);

        UserRegisterDto registerDto = new UserRegisterDto();
        registerDto.setName("testName");
        registerDto.setEmail("testEmail@email.com");
        registerDto.setPasswordEncoded("testPassword");
        MvcResult registerResult = this.mockMvc.perform(post("/api/v1/authentication/register")
                .content(new ObjectMapper().writeValueAsString(registerDto))
                .headers(registerHeaders))
            .andExpect(status().isOk())
            .andReturn();

        String authToken = registerResult.getResponse().getContentAsString();
        assertNotNull(authToken);
    }

    @Test
    public void testRegisterTwiceWithSameName_isBadRequest() throws Exception {
        // Login to get the token
        HttpHeaders registerHeaders = new HttpHeaders();
        registerHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        registerHeaders.setContentType(MediaType.APPLICATION_JSON);

        UserRegisterDto registerDto = new UserRegisterDto();
        registerDto.setName("testName");
        registerDto.setEmail("testEmail@email.com");
        registerDto.setPasswordEncoded("testPassword");
        MvcResult registerResult = this.mockMvc.perform(post("/api/v1/authentication/register")
                .content(new ObjectMapper().writeValueAsString(registerDto))
                .headers(registerHeaders))
            .andExpect(status().isOk())
            .andReturn();

        String authToken = registerResult.getResponse().getContentAsString();
        assertNotNull(authToken);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authToken);

        UserRegisterDto registerDto2 = new UserRegisterDto();
        registerDto2.setName("testName");
        registerDto2.setEmail("testEmail@email2.com");
        registerDto2.setPasswordEncoded("testPassword2");
        MvcResult registerResult2 = this.mockMvc.perform(post("/api/v1/authentication/register")
                .content(new ObjectMapper().writeValueAsString(registerDto2))
                .headers(registerHeaders))
            .andExpect(status().isBadRequest())
            .andReturn();
    }

    @Test
    void testRequestPasswordReset_WithValidEmail_IsOk() throws Exception {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/authentication/request_password_reset")
                .content("{\"email\":\"" + testuser.getEmail() + "\"}")
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Test
    void testRequestPasswordReset_WithInvalidEmail_IsNotFound() throws Exception {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/authentication/request_password_reset")
                .content("{\"email\":\"" + "thisEmailShouldNotExist@asdf.org" + "\"}")
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
    }

    @Test
    void testPasswordReset_WithValidRequest_IsOk() throws Exception {
        // given
        final String requestId = "abcdefghijklmnop";
        final String requestIdEncoded = PasswordEncoder.encode(requestId, "password_reset");
        PasswordResetRequest resetRequest = new PasswordResetRequest();
        resetRequest.setRequestTime(LocalDateTime.now());
        resetRequest.setId(requestIdEncoded);
        resetRequest.setUser(testuser.setId(testUserId));
        passwordResetRequestRepository.save(resetRequest);

        // when
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResetPasswordDto resetDto = new ResetPasswordDto();
        resetDto.setResetId(requestId);
        resetDto.setNewPassword("newPassword");
        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/authentication/password_reset")
                .content((new ObjectMapper()).writeValueAsString(resetDto))
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        // then
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        ApplicationUser updatedUser = userRepository.getReferenceById(testUserId);
        assertEquals(PasswordEncoder.encode("newPassword", testuser.getEmail()), updatedUser.getPasswordEncoded());
    }

    @Test
    void testPasswordReset_WithInvalidRequestId_IsNotFound() throws Exception {
        // given
        final String requestId = "abcdefghijklmnop";
        final String requestIdEncoded = PasswordEncoder.encode(requestId, "password_reset");
        PasswordResetRequest resetRequest = new PasswordResetRequest();
        resetRequest.setRequestTime(LocalDateTime.now());
        resetRequest.setId(requestIdEncoded);
        resetRequest.setUser(testuser.setId(testUserId));
        passwordResetRequestRepository.save(resetRequest);

        // when
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResetPasswordDto resetDto = new ResetPasswordDto();
        resetDto.setResetId("wrongrequestid");
        resetDto.setNewPassword("newPassword");
        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/authentication/password_reset")
                .content((new ObjectMapper()).writeValueAsString(resetDto))
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        // then
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
    }

    @Test
    void testPasswordReset_WithExpiredDate_IsUnauthorized() throws Exception {
        // given
        final String requestId = "abcdefghijklmnop";
        final String requestIdEncoded = PasswordEncoder.encode(requestId, "password_reset");
        PasswordResetRequest resetRequest = new PasswordResetRequest();
        resetRequest.setRequestTime(LocalDateTime.now().minusDays(10));
        resetRequest.setId(requestIdEncoded);
        resetRequest.setUser(testuser.setId(testUserId));
        passwordResetRequestRepository.save(resetRequest);

        // when
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResetPasswordDto resetDto = new ResetPasswordDto();
        resetDto.setResetId(requestId);
        resetDto.setNewPassword("newPassword");
        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/authentication/password_reset")
                .content((new ObjectMapper()).writeValueAsString(resetDto))
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        // then
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }
}
