package at.ac.tuwien.sepr.groupphase.backend.unittests.service;

import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.authentication.LoginDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.AuthenticationException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthenticationService;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class AuthenticationServiceTest {

    @Autowired
    AuthenticationService service;

    @Autowired
    UserRepository userRepository;

    private ApplicationUser testuser = new ApplicationUser()
        .setId(1L)
        .setNickname("testuser_authservicetest")
        .setEmail("test.user@authservice.test")
        .setPasswordEncoded(PasswordEncoder.encode("password", "test.user@authservice.test"));
    private ApplicationUser testuser2 = new ApplicationUser()
        .setId(2L)
        .setNickname("testuser2_authservicetest")
        .setEmail("another.test@authservice.net")
        .setPasswordEncoded(PasswordEncoder.encode("password2", "another.test@authservice.net"));

    @BeforeEach
    void beforeEach() {
        testuser = userRepository.save(testuser);
        testuser2 = userRepository.save(testuser2);
    }

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
    }

    @Test
    void testLogin_WithValidData_Returns() throws Exception {
        LoginDto dto = new LoginDto()
            .setEmail(testuser.getEmail())
            .setPassword("password");

        assertDoesNotThrow(() -> service.loginUser(dto));

        // cleanup
        service.logoutUser(testuser);
    }

    @Test
    void testLogin_WithInvalidEmail_ThrowsAuthenticationError() {
        LoginDto dto = new LoginDto()
            .setEmail("thisEmailDoesNotExist@authenticationtest.test")
            .setPassword("password");

        assertThrows(AuthenticationException.class, () -> service.loginUser(dto));
    }

    @Test
    void testLogin_WithInvalidPassword_ThrowsAuthenticationError() {
        LoginDto dto = new LoginDto()
            .setEmail(testuser.getEmail())
            .setPassword("thisIsAWrongPassword");

        assertThrows(AuthenticationException.class, () -> service.loginUser(dto));
    }

    @Test
    void testLogin_WithTwoUsers_Returns() {
        LoginDto dto1 = new LoginDto()
            .setEmail(testuser.getEmail())
            .setPassword("password");

        LoginDto dto2 = new LoginDto()
            .setEmail(testuser2.getEmail())
            .setPassword("password2");

        assertAll(
            () -> assertDoesNotThrow(() -> service.loginUser(dto1)),
            () -> assertDoesNotThrow(() -> service.loginUser(dto2))
        );
    }

    @Test
    void testLogout_WithValidSession_Returns() throws Exception {
        LoginDto dto = new LoginDto()
            .setEmail(testuser.getEmail())
            .setPassword("password");

        String authToken = service.loginUser(dto);

        // logout
        assertDoesNotThrow(() -> service.logoutUser(authToken));
    }

    @Test
    void testLogout_WithInvalidSession_ThrowsAuthenticationError() {
        assertThrows(AuthenticationException.class, () -> service.logoutUser("abcdefghijklmnop"));
    }

    @Test
    void testLogout_WithLoggedInUser_Returns() throws Exception {
        LoginDto dto = new LoginDto()
            .setEmail(testuser.getEmail())
            .setPassword("password");
        service.loginUser(dto);

        // logout
        assertDoesNotThrow(() -> service.logoutUser(testuser));
    }

    @Test
    void testLogout_WithNotLoggedInUser_ThrowsAuthenticationError() {
        ApplicationUser fakeUser = new ApplicationUser()
            .setId(1234567890L)
            .setPasswordEncoded(PasswordEncoder.encode("abcdefg", "fake@test.at"))
            .setEmail("fake@test.at")
            .setNickname("fakeuser");

        assertThrows(AuthenticationException.class, () -> service.logoutUser(fakeUser));
    }

    @Test
    void testVerifyAuthenticated_WithValidToken_Returns() throws Exception {
        LoginDto dto = new LoginDto()
            .setEmail(testuser.getEmail())
            .setPassword("password");
        service.loginUser(dto);
        String authToken = service.loginUser(dto);

        assertDoesNotThrow(() -> service.verifyAuthenticated(authToken));
    }

    @Test
    void testVerifyAuthenticated_WithInvalidToken_ThrowsAuthenticationError() {
        assertThrows(AuthenticationException.class, () -> service.verifyAuthenticated("invalidauthtoken"));
    }

    @Test
    void testVerifyAuthenticated_WithValidHeaders_Returns() throws Exception {
        LoginDto dto = new LoginDto()
            .setEmail(testuser.getEmail())
            .setPassword("password");
        service.loginUser(dto);
        String authToken = service.loginUser(dto);

        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", authToken);

        assertDoesNotThrow(() -> service.verifyAuthenticated(headers));
    }

    @Test
    void testVerifyAuthenticated_WithInvalidHeaders_ThrowsAuthenticationError() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", "invalidauthtoken");

        assertThrows(AuthenticationException.class, () -> service.verifyAuthenticated(headers));
    }

    @Test
    void testVerifyAuthenticated_WithMissingheader_ThrowsAuthenticationError() {
        HttpHeaders headers = new HttpHeaders();
        assertThrows(AuthenticationException.class, () -> service.verifyAuthenticated(headers));
    }

    @Test
    void testVerifyPassword_WithValidPassword_Returns() {
        assertDoesNotThrow(() -> service.verifyUserPassword(testuser.getId(), "password"));
    }

    @Test
    void testVerifyPassword_WithInvalidPassword_ThrowsAuthenticationError() {
        assertThrows(AuthenticationException.class, () -> service.verifyUserPassword(testuser.getId(), "wrongPassword"));
    }

    @Test
    void testVerifyPassword_WithInvalidUserId_ThrowsNotFoundError() {
        assertThrows(NotFoundException.class, () -> service.verifyUserPassword(100000000L, "password"));
    }

}
