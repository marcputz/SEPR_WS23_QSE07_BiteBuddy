package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.authentication.LoginDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.authentication.ResetPasswordDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserRegisterDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserSettingsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserUpdateEmailAndPasswordDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.PasswordResetRequest;
import at.ac.tuwien.sepr.groupphase.backend.repository.PasswordResetRequestRepository;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class UserEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetRequestRepository passwordResetRequestRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private ApplicationUser testuser = new ApplicationUser()
        .setId(-100L)
        .setNickname("authEndpointTest")
        .setEmail("test@authEndpoint.at")
        .setPasswordEncoded(PasswordEncoder.encode("password", "test@authEndpoint.at"));


    private ApplicationUser testuser2 = new ApplicationUser()
        .setId(-101L)
        .setNickname("secondTestUser")
        .setEmail("secondTest@authEndpoint.at")
        .setPasswordEncoded(PasswordEncoder.encode("password2", "secondTest@authEndpoint.at"));


    @BeforeEach
    public void beforeEach() {
        testuser = userRepository.save(testuser);
        testuser2 = userRepository.save(testuser2);
    }

    @AfterEach
    public void afterEach() {
        passwordResetRequestRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void testLogin_withValidData_isOk() throws Exception {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/user/login")
                .content((new ObjectMapper()).writeValueAsString(new LoginDto()
                    .setEmail(testuser.getEmail())
                    .setPassword("password")))
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        String authToken = response.getContentAsString();

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
            () -> assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType()),
            () -> assertNotNull(authToken),
            () -> assertEquals("Bearer ", authToken.substring(0, 7))
        );
    }

    @Test
    public void testLogin_withInvalidEmail_isAuthenticationError() throws Exception {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/user/login")
                .content((new ObjectMapper()).writeValueAsString(new LoginDto()
                    .setEmail("doesNotExist@shouldFail.net")
                    .setPassword("password")))
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }

    @Test
    public void testLogin_withInvalidPassword_isAuthenticationError() throws Exception {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/user/login")
                .content((new ObjectMapper()).writeValueAsString(new LoginDto()
                    .setEmail(testuser.getEmail())
                    .setPassword("thisIsAWrongPassword")))
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }

    @Test
    public void testLogoutApi_withValidToken_isOk() throws Exception {
        // given
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/user/login")
                .content((new ObjectMapper()).writeValueAsString(new LoginDto()
                    .setEmail(testuser.getEmail())
                    .setPassword("password")))
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        String authToken = response.getContentAsString();

        // when
        requestHeaders.set("Authorization", authToken);
        mvcResult = this.mockMvc.perform(post("/api/v1/user/logout")
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        response = mvcResult.getResponse();

        // should
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Test
    public void testLogoutApi_withInvalidToken_isBadRequest() throws Exception {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.set("Authorization", "Token nonexistingauthtoken");

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/user/logout")
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }

    @Test
    public void testUpdateEmailAndPassword_withValidData_isSuccessful() throws Exception {
        // Login to get the token
        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);

        MvcResult loginResult = this.mockMvc.perform(post("/api/v1/user/login")
                .content(new ObjectMapper().writeValueAsString(new LoginDto()
                    .setEmail(testuser.getEmail())
                    .setPassword("password")))
                .headers(loginHeaders))
            .andExpect(status().isOk())
            .andReturn();

        String authToken = loginResult.getResponse().getContentAsString();
        assertNotNull(authToken);

        // Prepare update data
        UserUpdateEmailAndPasswordDto updateDto = UserUpdateEmailAndPasswordDto.UserUpdateDtoBuilder.anUserUpdateDto()
            .withEmail("newemail@authEndpoint.at")
            .withCurrentPassword("password")
            .withNewPassword("newPassword")
            .build();

        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        updateHeaders.setContentType(MediaType.APPLICATION_JSON);
        updateHeaders.set("Authorization", authToken);

        // Perform update request
        MvcResult updateResult = this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/user/settings/authentication")
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

        MvcResult loginResult = this.mockMvc.perform(post("/api/v1/user/login")
                .content(new ObjectMapper().writeValueAsString(new LoginDto()
                    .setEmail(testuser.getEmail())
                    .setPassword("password")))
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
            .withCurrentPassword("password")
            .withNewPassword(newPassword)
            .build();

        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        updateHeaders.setContentType(MediaType.APPLICATION_JSON);
        updateHeaders.set("Authorization", authToken);

        // Perform update request
        MvcResult updateResult = this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/user/settings/authentication")
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

        MvcResult logoutResult = this.mockMvc.perform(post("/api/v1/user/logout")
                .headers(logoutHeaders))
            .andExpect(status().isOk())
            .andReturn();
        assertEquals(HttpStatus.OK.value(), logoutResult.getResponse().getStatus());

        // Perform another login with the new data
        MvcResult secondLoginResult = this.mockMvc.perform(post("/api/v1/user/login")
                .content(new ObjectMapper().writeValueAsString(new LoginDto()
                    .setEmail(newEmail)
                    .setPassword(newPassword)))
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

        MvcResult loginResult = this.mockMvc.perform(post("/api/v1/user/login")
                .content(new ObjectMapper().writeValueAsString(new LoginDto()
                    .setEmail(testuser.getEmail())
                    .setPassword("password")))
                .headers(loginHeaders))
            .andExpect(status().isOk())
            .andReturn();
        String authToken = loginResult.getResponse().getContentAsString();
        assertNotNull(authToken);
        UserUpdateEmailAndPasswordDto updateDto = UserUpdateEmailAndPasswordDto.UserUpdateDtoBuilder.anUserUpdateDto()
            .withEmail(testuser2.getEmail()) // Email already in use by secondTestUser
            .withCurrentPassword("password")
            .withNewPassword("newPassword")
            .build();
        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        updateHeaders.setContentType(MediaType.APPLICATION_JSON);
        updateHeaders.set("Authorization", authToken);

        // Perform update request
        MvcResult updateResult = this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/user/settings/authentication")
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
        MvcResult registerResult = this.mockMvc.perform(post("/api/v1/user/register")
                .content(new ObjectMapper().writeValueAsString(registerDto))
                .headers(registerHeaders))
            .andExpect(status().isCreated())
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
        MvcResult registerResult = this.mockMvc.perform(post("/api/v1/user/register")
                .content(new ObjectMapper().writeValueAsString(registerDto))
                .headers(registerHeaders))
            .andExpect(status().isCreated())
            .andReturn();

        String authToken = registerResult.getResponse().getContentAsString();
        assertNotNull(authToken);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authToken);

        UserRegisterDto registerDto2 = new UserRegisterDto();
        registerDto2.setName("testName");
        registerDto2.setEmail("testEmail@email2.com");
        registerDto2.setPasswordEncoded("testPassword2");
        MvcResult registerResult2 = this.mockMvc.perform(post("/api/v1/user/register")
                .content(new ObjectMapper().writeValueAsString(registerDto2))
                .headers(registerHeaders))
            .andExpect(status().isConflict())
            .andReturn();
    }

    @Test
    void testRequestPasswordReset_WithValidEmail_IsAccepted() throws Exception {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/user/request_password_reset")
                .content("{\"email\":\"" + testuser.getEmail() + "\"}")
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.ACCEPTED.value(), response.getStatus());
    }

    @Test
    void testRequestPasswordReset_WithInvalidEmail_IsNotFound() throws Exception {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/user/request_password_reset")
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
        resetRequest.setUser(testuser.setId(testuser.getId()));
        passwordResetRequestRepository.save(resetRequest);

        // when
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResetPasswordDto resetDto = new ResetPasswordDto();
        resetDto.setResetId(requestId);
        resetDto.setNewPassword("newPassword");
        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/user/password_reset")
                .content((new ObjectMapper()).writeValueAsString(resetDto))
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        // then
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        ApplicationUser updatedUser = userRepository.getReferenceById(testuser.getId());
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
        resetRequest.setUser(testuser.setId(testuser.getId()));
        passwordResetRequestRepository.save(resetRequest);

        // when
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResetPasswordDto resetDto = new ResetPasswordDto();
        resetDto.setResetId("wrongrequestid");
        resetDto.setNewPassword("newPassword");
        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/user/password_reset")
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
        resetRequest.setUser(testuser.setId(testuser.getId()));
        passwordResetRequestRepository.save(resetRequest);

        // when
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResetPasswordDto resetDto = new ResetPasswordDto();
        resetDto.setResetId(requestId);
        resetDto.setNewPassword("newPassword");
        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/user/password_reset")
                .content((new ObjectMapper()).writeValueAsString(resetDto))
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        // then
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }
}
