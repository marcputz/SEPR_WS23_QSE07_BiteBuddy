package at.ac.tuwien.sepr.groupphase.backend.unittests.service;

import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.LoginDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.AuthenticationException;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthenticationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class AuthenticationServiceTest implements TestData {

    @Autowired
    AuthenticationService service;

    @Autowired
    UserRepository user;

    private ApplicationUser testuser = new ApplicationUser()
        .setId(1L)
        .setNickname("testuser_authservicetest")
        .setEmail("test.user@authservice.test")
        .setPasswordEncoded(PasswordEncoder.encode("password", "test.user@authservice.test"));

    @BeforeEach
    void beforeEach() {
        long newId = user.save(testuser).getId();
        testuser.setId(newId);
    }

    @AfterEach
    void afterEach() {
        user.deleteById(testuser.getId());
    }

    @Test
    void testLogin_WithValidData_Returns() throws Exception {
        LoginDto dto = LoginDto.LoginDtobuilder.anLoginDto()
            .withEmail(testuser.getEmail())
            .withPassword("password")
            .build();

        assertDoesNotThrow(() -> service.loginUser(dto));

        // cleanup
        service.logoutUser(testuser);
    }

    @Test
    void testLogin_WithInvalidData_ThrowsAuthenticationError() {
        LoginDto dto = LoginDto.LoginDtobuilder.anLoginDto()
            .withEmail("thisEmailDoesNotExist@authenticationtest.test")
            .withPassword("password")
            .build();

        assertThrows(AuthenticationException.class, () -> service.loginUser(dto));
    }

    @Test
    void testLogout_WithValidSession_Returns() throws Exception {
        LoginDto dto = LoginDto.LoginDtobuilder.anLoginDto()
            .withEmail(testuser.getEmail())
            .withPassword("password")
            .build();
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
        LoginDto dto = LoginDto.LoginDtobuilder.anLoginDto()
            .withEmail(testuser.getEmail())
            .withPassword("password")
            .build();
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
        LoginDto dto = LoginDto.LoginDtobuilder.anLoginDto()
            .withEmail(testuser.getEmail())
            .withPassword("password")
            .build();
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
        LoginDto dto = LoginDto.LoginDtobuilder.anLoginDto()
            .withEmail(testuser.getEmail())
            .withPassword("password")
            .build();
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
        assertThrows(UserNotFoundException.class, () -> service.verifyUserPassword(100000000L, "password"));
    }

}
