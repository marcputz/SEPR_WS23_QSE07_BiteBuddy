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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


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

    private ApplicationUser testuser = new ApplicationUser()
        .setEmail("test@test.org")
        .setNickname("testuser_passwordresetservice")
        .setPasswordEncoded(PasswordEncoder.encode("password", "test@test.org"));

    @BeforeEach
    void init() {
        passwordResetRequestRepository.deleteAll();
        userRepository.deleteAll();
        ApplicationUser createdUser = userRepository.save(testuser);
        testuser.setId(createdUser.getId());
    }

    @Test
    void testRequestPasswordReset_WithValidEmail_DoesNotThrow() {
        assertDoesNotThrow(() -> service.requestPasswordReset(testuser.getEmail()));
    }

    @Test
    void testRequestPasswordReset_WithInvalidEmail_DoesThrow() {
        assertThrows(UserNotFoundException.class, () -> service.requestPasswordReset("doesNotExist@asdf.com"));
    }

    @Test
    void testPasswordReset_WithValidRequest_DoesNotThrow() {
        // given
        final String requestId = "abcdefghijklmnop";
        final String requestIdEncoded = PasswordEncoder.encode(requestId, "password_reset");
        PasswordResetRequest resetRequest = new PasswordResetRequest();
        resetRequest.setRequestTime(LocalDateTime.now());
        resetRequest.setId(requestIdEncoded);
        resetRequest.setUser(testuser);
        passwordResetRequestRepository.save(resetRequest);

        // when
        ResetPasswordDto dto = new ResetPasswordDto();
        dto.setResetId(requestId);
        dto.setNewPassword("newPassword");

        // then
        assertDoesNotThrow(() -> service.resetPassword(dto));
        ApplicationUser updatedUser = userRepository.getReferenceById(testuser.getId());
        assertEquals(PasswordEncoder.encode("newPassword", testuser.getEmail()), updatedUser.getPasswordEncoded());
    }

    @Test
    void testPasswordReset_WithInvalidRequestId_ThrowsNotFound() {
        // given
        final String requestId = "abcdefghijklmnop";
        final String requestIdEncoded = PasswordEncoder.encode(requestId, "password_reset");
        PasswordResetRequest resetRequest = new PasswordResetRequest();
        resetRequest.setRequestTime(LocalDateTime.now());
        resetRequest.setId(requestIdEncoded);
        resetRequest.setUser(testuser);
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
        final String requestId = "abcdefghijklmnop";
        final String requestIdEncoded = PasswordEncoder.encode(requestId, "password_reset");
        PasswordResetRequest resetRequest = new PasswordResetRequest();
        resetRequest.setRequestTime(LocalDateTime.now().minusDays(10));
        resetRequest.setId(requestIdEncoded);
        resetRequest.setUser(testuser);
        passwordResetRequestRepository.save(resetRequest);

        // when
        ResetPasswordDto dto = new ResetPasswordDto();
        dto.setResetId(requestId);
        dto.setNewPassword("newPassword");

        // then
        assertThrows(AuthenticationException.class, () -> service.resetPassword(dto));
    }
}
