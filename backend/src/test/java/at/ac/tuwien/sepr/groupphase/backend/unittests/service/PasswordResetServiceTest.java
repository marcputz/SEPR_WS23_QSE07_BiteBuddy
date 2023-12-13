package at.ac.tuwien.sepr.groupphase.backend.unittests.service;

import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ResetPasswordDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.PasswordResetRequest;
import at.ac.tuwien.sepr.groupphase.backend.exception.AuthenticationException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.PasswordResetRequestRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.PasswordResetService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class PasswordResetServiceTest {

    @Autowired
    private PasswordResetService service;

    @Autowired
    private PasswordResetRequestRepository passwordResetRequestRepository;
    @Autowired
    private UserRepository userRepository;

    private ApplicationUser TESTUSER = new ApplicationUser()
        .setEmail("test@test.org")
        .setNickname("testuser_passwordresetservice")
        .setPasswordEncoded(PasswordEncoder.encode("password", "test@test.org"));

    @BeforeEach
    void init() {
        passwordResetRequestRepository.deleteAll();
        userRepository.deleteAll();
        ApplicationUser createdUser = userRepository.save(TESTUSER);
        TESTUSER.setId(createdUser.getId());
    }

    @Test
    void testRequestPasswordReset_WithValidEmail_DoesNotThrow(){
        assertDoesNotThrow(() -> service.requestPasswordReset(TESTUSER.getEmail()));
    }

    @Test
    void testRequestPasswordReset_WithInvalidEmail_DoesThrow(){
        assertThrows(UserNotFoundException.class, () -> service.requestPasswordReset("doesNotExist@asdf.com"));
    }

    @Test
    void testPasswordReset_WithValidRequest_DoesNotThrow() {
        // given
        final String REQUEST_ID = "abcdefghijklmnop";
        final String REQUEST_ID_ENCODED = PasswordEncoder.encode(REQUEST_ID, "password_reset");
        PasswordResetRequest resetRequest = new PasswordResetRequest();
        resetRequest.setRequestTime(LocalDateTime.now());
        resetRequest.setId(REQUEST_ID_ENCODED);
        resetRequest.setUser(TESTUSER);
        passwordResetRequestRepository.save(resetRequest);

        // when
        ResetPasswordDto dto = new ResetPasswordDto();
        dto.setResetId(REQUEST_ID);
        dto.setNewPassword("newPassword");

        // then
        assertDoesNotThrow(() -> service.resetPassword(dto));
        ApplicationUser updatedUser = userRepository.getReferenceById(TESTUSER.getId());
        assertEquals(PasswordEncoder.encode("newPassword", TESTUSER.getEmail()), updatedUser.getPasswordEncoded());
    }

    @Test
    void testPasswordReset_WithInvalidRequestId_ThrowsNotFound() {
        // given
        final String REQUEST_ID = "abcdefghijklmnop";
        final String REQUEST_ID_ENCODED = PasswordEncoder.encode(REQUEST_ID, "password_reset");
        PasswordResetRequest resetRequest = new PasswordResetRequest();
        resetRequest.setRequestTime(LocalDateTime.now());
        resetRequest.setId(REQUEST_ID_ENCODED);
        resetRequest.setUser(TESTUSER);
        passwordResetRequestRepository.save(resetRequest);

        // when
        ResetPasswordDto dto = new ResetPasswordDto();
        dto.setResetId("invalidrequestid");
        dto.setNewPassword("newPassword");

        // then
        assertThrows(NotFoundException.class, () -> service.resetPassword(dto));
    }

    @Test
    void testPasswordReset_WithExpiredRequest_DoesNotThrow() {
        // given
        final String REQUEST_ID = "abcdefghijklmnop";
        final String REQUEST_ID_ENCODED = PasswordEncoder.encode(REQUEST_ID, "password_reset");
        PasswordResetRequest resetRequest = new PasswordResetRequest();
        resetRequest.setRequestTime(LocalDateTime.now().minusDays(10));
        resetRequest.setId(REQUEST_ID_ENCODED);
        resetRequest.setUser(TESTUSER);
        passwordResetRequestRepository.save(resetRequest);

        // when
        ResetPasswordDto dto = new ResetPasswordDto();
        dto.setResetId(REQUEST_ID);
        dto.setNewPassword("newPassword");

        // then
        assertThrows(AuthenticationException.class, () -> service.resetPassword(dto));
    }
}
