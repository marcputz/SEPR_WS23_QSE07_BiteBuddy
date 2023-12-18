package at.ac.tuwien.sepr.groupphase.backend.unittests.service;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;
    private ApplicationUser testUser;
    private long testUserId;

    @BeforeEach
    public void generateTestUser() {
        testUser = new ApplicationUser().setId(-1L).setEmail("max.mustermann@test.at")
            .setPasswordEncoded("ba527ca265c37cf364b057b4f412d175f79d363e0e15d709097f188a4fe979ba2cc1c048e1c97da7804465cef5f8abe7") // "password"
            .setNickname("maxmuster");
        testUserId = userRepository.save(testUser).getId();
    }

    private ApplicationUser secondTestUser;
    private long secondTestUserId;

    private void generateSecondTestUser() {
        secondTestUser = new ApplicationUser().setId(-2L).setEmail("jane.doe@test.at")
            .setPasswordEncoded("encodedPassword") // Example encoded password
            .setNickname("janedoe");
        secondTestUserId = userRepository.save(secondTestUser).getId();
    }

    @AfterEach
    public void deleteTestUser() {
        userRepository.deleteById(testUserId);
    }

    @Test
    public void testGetUserByEmail_Successful() throws Exception {
        ApplicationUser user = userService.getUserByEmail("max.mustermann@test.at");

        assertAll(
            () -> assertEquals("maxmuster", user.getNickname(), "nickname not matching"),
            () -> assertEquals("max.mustermann@test.at", user.getEmail(), "email not matching"),
            () -> assertEquals("ba527ca265c37cf364b057b4f412d175f79d363e0e15d709097f188a4fe979ba2cc1c048e1c97da7804465cef5f8abe7", user.getPasswordEncoded(),
                "password not matching"),
            () -> assertNotNull(user.getCreatedAt(), "entity does not have create date"),
            () -> assertNotNull(user.getUpdatedAt(), "entity does not have update date")
        );
    }

    @Test
    public void testGetUserByEmail_NotFound_ThrowsUserNotFoundException() {
        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserByEmail("test@shouldnotexist.at");
        });
    }

    @Test
    public void testGetUserByNickname_Successful() throws Exception {
        ApplicationUser user = userService.getUserByNickname("maxmuster");

        assertAll(
            () -> assertEquals("maxmuster", user.getNickname(), "nickname not matching"),
            () -> assertEquals("max.mustermann@test.at", user.getEmail(), "email not matching"),
            () -> assertEquals("ba527ca265c37cf364b057b4f412d175f79d363e0e15d709097f188a4fe979ba2cc1c048e1c97da7804465cef5f8abe7", user.getPasswordEncoded(),
                "password not matching"),
            () -> assertNotNull(user.getCreatedAt(), "entity does not have create date"),
            () -> assertNotNull(user.getUpdatedAt(), "entity does not have update date")
        );
    }

    @Test
    public void testGetUserByNickname_NotFound_ThrowsUserNotFoundException() {
        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserByNickname("shouldNotExist");
        });
    }

    @Test
    public void testUpdateUser_Successful() throws Exception {
        ApplicationUser userToBeUpdated = userService.getUserByEmail("max.mustermann@test.at");
        userToBeUpdated.setEmail("updated.email@test.at");
        userToBeUpdated.setNickname("updatedNickname");

        ApplicationUser result = userService.updateApplicationUser(userToBeUpdated);

        assertAll(
            () -> assertEquals("updated.email@test.at", result.getEmail(), "Email not updated correctly"),
            () -> assertEquals("updatedNickname", result.getNickname(), "Nickname not updated correctly"),
            () -> assertEquals(testUser.getPasswordEncoded(), result.getPasswordEncoded(), "Password should not have changed"),
            () -> assertNotNull(result.getUpdatedAt(), "Update date should be set")
        );
    }

    @Test
    public void testUpdateUser_NonExistentUser_ThrowsUserNotFoundException() {
        ApplicationUser nonExistentUser = new ApplicationUser()
            .setId(Long.MIN_VALUE) // An ID that does not exist in the database
            .setEmail("nonexistent.email@test.at")
            .setNickname("nonexistentNickname");

        assertThrows(UserNotFoundException.class, () -> {
            userService.updateApplicationUser(nonExistentUser);
        });
    }

    @Test
    public void testUpdateUser_WithMultipleValidationErrors_ThrowsValidationException() {
        ApplicationUser invalidUser = new ApplicationUser()
            .setId(testUserId)
            .setEmail(null) // Null Email
            .setPasswordEncoded(null) // Null Password
            .setNickname(null); // Null Nickname
        ValidationException validationException = assertThrows(ValidationException.class, () -> userService.updateApplicationUser(invalidUser));
        List<String> expectedErrors = List.of(
            "Email is required",
            "Password is required",
            "Nickname is required");
        List<String> actualErrors = validationException.errors();
        assertTrue(actualErrors.containsAll(expectedErrors), "Validation errors should contain all expected errors");
    }

    @Test
    public void testUpdateUser_InvalidEmailFormat_ThrowsValidationException() {
        ApplicationUser userWithInvalidEmail = testUser.setEmail("invalidEmailFormat"); // Invalid email format
        ValidationException validationException = assertThrows(ValidationException.class, () -> userService.updateApplicationUser(userWithInvalidEmail));
        assertTrue(validationException.errors().contains("Invalid email format"), "Validation error should contain invalid email format message");
    }

    @Test
    public void testUpdateUser_TooLongEmailOrNickname_ThrowsValidationException() {
        String longEmail = "a".repeat(256) + "@test.com"; // Email longer than 255 characters
        String longNickname = "a".repeat(256); // Nickname longer than 255 characters
        ApplicationUser userWithLongEmailAndNickname = testUser.setEmail(longEmail)
            .setNickname(longNickname);
        ValidationException validationException = assertThrows(ValidationException.class,
            () -> userService.updateApplicationUser(userWithLongEmailAndNickname));
        List<String> expectedErrors = List.of(
            "Email cannot be longer than 255 characters",
            "Nickname cannot be longer than 255 characters");
        assertTrue(validationException.errors().containsAll(expectedErrors),
            "Validation errors should contain messages for excessively long email and nickname");
    }

    @Test
    public void testUpdateUser_DuplicateNicknameAndEmail_ThrowsConflictException() {
        generateSecondTestUser();
        testUser.setEmail(secondTestUser.getEmail());
        testUser.setNickname(secondTestUser.getNickname());
        ConflictException conflictException = assertThrows(ConflictException.class, () -> userService.updateApplicationUser(testUser),
            "Expected ConflictException for duplicate email");
        List<String> expectedErrors = List.of(
            "Email '" + testUser.getEmail() + "' is already in use",
            "Nickname '" + testUser.getNickname() + "' is already in use");
        assertTrue(conflictException.errors().containsAll(expectedErrors), "Conflict errors should contain messages for duplicate email and nickname");
    }

}
