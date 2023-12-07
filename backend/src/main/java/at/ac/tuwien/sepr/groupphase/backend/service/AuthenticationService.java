package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.auth.AuthTokenUtils;
import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.auth.SessionManager;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.LoginDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.AuthenticationException;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
     * Verifies whether the provided user ID and password correspond to valid credentials.
     * This method can be used for operations where a user's current password needs to be confirmed,
     * such as before changing user settings.
     *
     * @param id       The ID of the user whose credentials are to be checked.
     * @param password The password to validate against the user's stored password.
     * @return true if the user's ID and password are correct; false otherwise.
     * @throws UserNotFoundException if no user is found with the provided ID.
     */
    public boolean checkCredentials(Long id, String password) throws UserNotFoundException {
        ApplicationUser user = userService.getUserById(id);

        // Encode the provided password to compare with the stored password hash
        String encodedPassword = PasswordEncoder.encode(password, user.getEmail());

        // Return true if the passwords match, false otherwise
        return user.checkPasswordMatch(encodedPassword);
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
        Long userId = SessionManager.getInstance().getUserFromAuthToken(authToken);
        if (userId == null) {
            throw new AuthenticationException("Token not authenticated");
        }
    }

    /**
     * Verifies if a http header contains valid authentication data
     *
     * @author Marc Putz
     * @param headers the http headers of the request to authenticate
     * @throws AuthenticationException if the headers do contain no or invalid authentication data
     */
    public void verifyAuthenticated(HttpHeaders headers) throws AuthenticationException {
        String authToken = headers.getFirst("authorization");

        if (authToken == null) {
            throw new AuthenticationException("Session not authenticated. Please log in.");
        }

        verifyAuthenticated(authToken);
    }
}
