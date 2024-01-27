package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.auth.SessionManager;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.utils.AuthTokenUtils;
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

    private final ApplicationUser testuser = new ApplicationUser()
        .setId(1L)
        .setNickname("maxmuster")
        .setEmail("max.mustermann@test.at");
    private final String testuserAuthtoken = AuthTokenUtils.createToken(testuser);

    @Order(1)
    @Test
    public void testStartAndStopSession_WithValidData_Returns() {
        assertTrue(SessionManager.getInstance().startUserSession(testuser.getId(), testuserAuthtoken));
        assertTrue(SessionManager.getInstance().stopUserSession(testuserAuthtoken));
    }

    @Test
    public void testStartSession_WithNoToken_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            SessionManager.getInstance().startUserSession(testuser.getId(), null);
        });
    }

    @Test
    public void testStop_WithNonExistingSession_ReturnsNull() {
        String nonExistingSessionToken = AuthTokenUtils.createToken(new ApplicationUser().setId(-1000L).setNickname("test"));
        assertFalse(SessionManager.getInstance().stopUserSession(nonExistingSessionToken));
    }

    @Test
    public void testGetUser_FromStartedSession_Returns() {
        assertTrue(SessionManager.getInstance().startUserSession(testuser.getId(), testuserAuthtoken));
        assertEquals(testuser.getId(), SessionManager.getInstance().getUserFromAuthToken(testuserAuthtoken));

        // cleanup
        SessionManager.getInstance().stopUserSession(testuserAuthtoken);
    }

    @Test
    public void testGetUser_FromNonExistingSession_ReturnsNull() {
        String nonExistingSessionToken = AuthTokenUtils.createToken(new ApplicationUser().setId(-1000L).setNickname("test"));
        assertNull(SessionManager.getInstance().getUserFromAuthToken(nonExistingSessionToken));
    }

    @Test
    public void testGetAuthToken_FromStartedSession_Returns() {
        assertTrue(SessionManager.getInstance().startUserSession(testuser.getId(), testuserAuthtoken));
        assertEquals(testuserAuthtoken, SessionManager.getInstance().getAuthTokenForUser(testuser.getId()));

        // cleanup
        SessionManager.getInstance().stopUserSession(testuserAuthtoken);
    }

    @Test
    public void testGetAuthToken_FromNonExistingSession_ReturnsNull() {
        ApplicationUser nonExistingSessionUser = new ApplicationUser().setId(-1000L).setNickname("test");
        assertNull(SessionManager.getInstance().getAuthTokenForUser(nonExistingSessionUser.getId()));
    }

}
