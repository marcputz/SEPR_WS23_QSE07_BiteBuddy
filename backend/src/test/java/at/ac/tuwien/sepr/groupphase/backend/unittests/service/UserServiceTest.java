package at.ac.tuwien.sepr.groupphase.backend.unittests.service;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotFoundException;
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

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private long testUserId;

    @BeforeEach
    public void generateTestUser() {
        ApplicationUser user1 = new ApplicationUser().setId(-1L).setEmail("max.mustermann@test.at")
            .setPasswordEncoded("ba527ca265c37cf364b057b4f412d175f79d363e0e15d709097f188a4fe979ba2cc1c048e1c97da7804465cef5f8abe7") // "password"
            .setNickname("maxmuster");
        testUserId = userRepository.save(user1).getId();
    }

    @AfterEach
    public void deleteTestUser() {
        userRepository.deleteById(testUserId);
    }

    @Test
    public void testGetUserByEmail() throws Exception {
        ApplicationUser user = userService.getUserByEmail("max.mustermann@test.at");

        assertAll(
            () -> assertEquals("maxmuster", user.getNickname(), "nickname not matching"),
            () -> assertEquals("max.mustermann@test.at", user.getEmail(), "email not matching"),
            () -> assertEquals("ba527ca265c37cf364b057b4f412d175f79d363e0e15d709097f188a4fe979ba2cc1c048e1c97da7804465cef5f8abe7", user.getPasswordEncoded(), "password not matching"),
            () -> assertNotNull(user.getCreatedAt(), "entity does not have create date"),
            () -> assertNotNull(user.getUpdatedAt(), "entity does not have update date")
        );
    }

    @Test
    public void testGetUserByEmailNotFound() {
        assertThrows(UserNotFoundException.class, () -> { userService.getUserByEmail("test@shouldnotexist.at"); });
    }

    @Test
    public void testGetUserByNickname() throws Exception {
        ApplicationUser user = userService.getUserByNickname("maxmuster");

        assertAll(
            () -> assertEquals("maxmuster", user.getNickname(), "nickname not matching"),
            () -> assertEquals("max.mustermann@test.at", user.getEmail(), "email not matching"),
            () -> assertEquals("ba527ca265c37cf364b057b4f412d175f79d363e0e15d709097f188a4fe979ba2cc1c048e1c97da7804465cef5f8abe7", user.getPasswordEncoded(), "password not matching"),
            () -> assertNotNull(user.getCreatedAt(), "entity does not have create date"),
            () -> assertNotNull(user.getUpdatedAt(), "entity does not have update date")
        );
    }

    @Test
    public void testGetUserByNicknameNotFound() {
        assertThrows(UserNotFoundException.class, () -> { userService.getUserByNickname("shouldNotExist"); });
    }
}
