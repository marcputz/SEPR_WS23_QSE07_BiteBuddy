package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.auth.AuthTokenUtils;
import at.ac.tuwien.sepr.groupphase.backend.auth.SessionManager;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class SessionManagerTest {

    private final ApplicationUser TESTUSER = new ApplicationUser()
        .setId(1L)
        .setNickname("maxmuster")
        .setEmail("max.mustermann@test.at");
    private final String TESTUSER_AUTHTOKEN = AuthTokenUtils.createToken(TESTUSER);

    @Order(1)
    @Test
    public void testStartAndStopSessionWithValidData() {
        assertTrue(SessionManager.getInstance().startUserSession(TESTUSER.getId(), TESTUSER_AUTHTOKEN));
        assertTrue(SessionManager.getInstance().stopUserSession(TESTUSER_AUTHTOKEN));
    }

    @Test
    public void testStartSessionWithNoTokenThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            SessionManager.getInstance().startUserSession(TESTUSER.getId(), null);
        });
    }

    @Test
    public void testStopNonExistingSessionReturnsNull() {
        String nonExistingSessionToken = AuthTokenUtils.createToken(new ApplicationUser().setId(-1000L).setNickname("test"));
        assertFalse(SessionManager.getInstance().stopUserSession(nonExistingSessionToken));
    }

    @Test
    public void testGetUserFromStartedSession() {
        assertTrue(SessionManager.getInstance().startUserSession(TESTUSER.getId(), TESTUSER_AUTHTOKEN));
        assertEquals(TESTUSER.getId(), SessionManager.getInstance().getUserFromAuthToken(TESTUSER_AUTHTOKEN));

        // cleanup
        SessionManager.getInstance().stopUserSession(TESTUSER_AUTHTOKEN);
    }

    @Test
    public void testGetUserFromNonExistingSessionReturnsNull() {
        String nonExistingSessionToken = AuthTokenUtils.createToken(new ApplicationUser().setId(-1000L).setNickname("test"));
        assertNull(SessionManager.getInstance().getUserFromAuthToken(nonExistingSessionToken));
    }

    @Test
    public void testGetAuthTokenFromStartedSession() {
        assertTrue(SessionManager.getInstance().startUserSession(TESTUSER.getId(), TESTUSER_AUTHTOKEN));
        assertEquals(TESTUSER_AUTHTOKEN, SessionManager.getInstance().getAuthTokenForUser(TESTUSER.getId()));

        // cleanup
        SessionManager.getInstance().stopUserSession(TESTUSER_AUTHTOKEN);
    }

    @Test
    public void testGetAuthtokenFromNonExistingSessionReturnsNull() {
        ApplicationUser nonExistingSessionUser = new ApplicationUser().setId(-1000L).setNickname("test");
        assertNull(SessionManager.getInstance().getAuthTokenForUser(nonExistingSessionUser.getId()));
    }

}
