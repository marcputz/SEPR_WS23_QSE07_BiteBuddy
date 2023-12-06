package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.auth.AuthTokenUtils;
import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.auth.SessionManager;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.LoginDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.AuthenticationException;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class to handle user authentication
 *
 * @author Marc Putz
 */
@Service
public class AuthenticationService {

    private final UserService userService;

    @Autowired
    public AuthenticationService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Logs in a user (checks for correct email and password)
     *
     * @param loginDto the DTO containing login data (email and password)
     * @return an authentication token for the user's session
     * @throws AuthenticationException if the login data is invalid or the session cannot be started
     */
    public String loginUser(LoginDto loginDto) throws AuthenticationException {
        try {
            // Look for user
            ApplicationUser user = userService.getUserByEmail(loginDto.getEmail());

            // Check if password exists
            if (loginDto.getPassword() == null) {
                throw new AuthenticationException("No password provided");
            }

            // encode password
            String encodedPassword = PasswordEncoder.encode(loginDto.getPassword(), loginDto.getEmail());

            // Check password data
            if (user.checkPasswordMatch(encodedPassword)) {

                // login
                return this.loginUser(user);

            } else {
                // wrong password
                throw new AuthenticationException("Wrong Password");
            }
        } catch (UserNotFoundException ex) {
            // login email not found
            throw new AuthenticationException("User '" + loginDto.getEmail() + "' does not exist");
        }
    }

    /**
     * Logs in a user (WARNING: does not authenticate, only authorizes)
     *
     * @param user the user to login
     * @return an authentication token for the user's session
     * @throws AuthenticationException if the session cannot be started
     */
    private String loginUser(ApplicationUser user) throws AuthenticationException {
        // Create jwt token
        String authToken = AuthTokenUtils.createToken(user);

        // Register user session
        if (!SessionManager.getInstance().startUserSession(user.getId(), authToken)) {
            throw new AuthenticationException("Cannot start user session");
        }

        return authToken;
    }

    /**
     * Logs out a user by stopping their session
     *
     * @author Marc Putz
     * @param user the user to logout
     * @throws AuthenticationException if user not logged in
     */
    public void logoutUser(ApplicationUser user) throws AuthenticationException {
        String authTokenToLogOut = SessionManager.getInstance().getAuthTokenForUser(user.getId());

        if (authTokenToLogOut != null) {
            logoutUser(authTokenToLogOut);
        } else {
            throw new AuthenticationException("Cannot log out: User has no session");
        }
    }

    /**
     * Logs out a user by stopping the token session.
     *
     * @author Marc Putz
     * @param authToken the authentication token to logout
     * @throws AuthenticationException if token invalid or not logged in
     */
    public void logoutUser(String authToken) throws AuthenticationException {
        // check token validity
        if (!AuthTokenUtils.isValid(authToken)) {
            throw new AuthenticationException("Cannot log out: Invalid session token");
        }

        // stop user session
        if (!SessionManager.getInstance().stopUserSession(authToken)) {
            throw new AuthenticationException("Cannot log out: No session for token found");
        }
    }

    /**
     * Verifies if an authentication token is really authenticated (meaning, token has an open session registered).
     *
     * @author Marc Putz
     * @param authToken the authentication token to check
     * @returns {@code true}, if authenticated. {@code false}, if no session found
     */
    public boolean isAuthenticated(String authToken) {
        return SessionManager.getInstance().getUserFromAuthToken(authToken) != null;
    }

    /**
     * Verifies if an authentication token is really authenticated (meaning, token has an open session registered).
     *
     * @author Marc Putz
     * @param authToken the authentication token to check
     * @throws AuthenticationException if token is not registered as an open session
     */
    public void verifyAuthenticated(String authToken) throws AuthenticationException {
        if (SessionManager.getInstance().getUserFromAuthToken(authToken) != null) {
            throw new AuthenticationException("Token not authenticated");
        }
    }
}
