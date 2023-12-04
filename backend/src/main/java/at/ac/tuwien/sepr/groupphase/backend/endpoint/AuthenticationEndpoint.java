package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.auth.AuthTokenUtils;
import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.auth.SessionManager;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.LoginDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserRegisterDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.AuthenticationException;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.lang.invoke.MethodHandles;

/**
 * REST endpoint for user authentication
 */
@RestController
@RequestMapping(value = "/api/v1/authentication")
public class AuthenticationEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserService userService;

    public AuthenticationEndpoint(UserService userService) {
        this.userService = userService;
    }

    /**
     * Logs in a user, using data from {@code userLoginDto}.
     *
     * @author Marc Putz
     * @param userLoginDto login data from the client.
     * @return a ResponseEntity to send back to the client, containing the JWT authentication token.
     * @throws AuthenticationException if the login data could not be matched to a user or user could not be authenticated.
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDto userLoginDto) throws AuthenticationException {
        LOGGER.trace("login({})", userLoginDto);

        try {
            // Look for user
            // Check if password exists
            if (userLoginDto.getPassword() == null) {
                throw new AuthenticationException("No password provided");
            }
            // encode password
            String encodedPassword = PasswordEncoder.encode(userLoginDto.getPassword(), userLoginDto.getEmail());

            ApplicationUser user = userService.getUserByEmail(userLoginDto.getEmail());

            // Check password data
            if (user.checkPasswordMatch(encodedPassword)) {

                // Create jwt token
                String authToken = AuthTokenUtils.createToken(user.getId(), user.getNickname());

                // Register user session
                if (!SessionManager.getInstance().startUserSession(user.getId(), authToken)) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to start user session");
                }

                // Return jwt auth token
                return new ResponseEntity<String>(authToken, HttpStatus.OK);

            } else {
                // wrong password
                throw new AuthenticationException("Wrong Password");
            }
        } catch (UserNotFoundException e) {
            // login email not found
            throw new AuthenticationException("User '" + userLoginDto.getEmail() + "' does not exist");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserRegisterDto registerDto) throws AuthenticationException {
        LOGGER.trace("register({})", registerDto);
        String encodedPassword = PasswordEncoder.encode(registerDto.getPasswordEncoded(), registerDto.getEmail());
        LoginDto loginDto = new LoginDto();
        loginDto.setPassword(registerDto.getPasswordEncoded());
        loginDto.setEmail(registerDto.getEmail());
        registerDto.setPasswordEncoded(encodedPassword);
        userService.create(registerDto);
        return login(loginDto);
    }

    /**
     * Logs out a user, using the JWT authentication token in the HTTP header
     *
     * @author Marc Putz
     * @param headers header of the HTTP request
     * @return a ResponseEntity to send back to the client, containing a boolean value indicating if the logout operation was successful.
     */
    @PostMapping("/logout")
    public ResponseEntity<Boolean> logout(@RequestHeader HttpHeaders headers) {

        // retrieve token from authorization header
        String authToken = headers.getFirst("authorization");
        if (authToken == null) {
            return new ResponseEntity<Boolean>(false, HttpStatus.BAD_REQUEST);
        }

        // check token validity
        if (!AuthTokenUtils.isValid(authToken)) {
            return new ResponseEntity<Boolean>(false, HttpStatus.BAD_REQUEST);
        }

        // stop user session
        if (!SessionManager.getInstance().stopUserSession(authToken)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to stop user session");
        }

        return new ResponseEntity<Boolean>(true, HttpStatus.OK);
    }
}
