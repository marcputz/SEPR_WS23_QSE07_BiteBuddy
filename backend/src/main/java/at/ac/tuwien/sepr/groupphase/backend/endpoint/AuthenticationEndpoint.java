package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.auth.AuthTokenUtils;
import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.auth.SessionManager;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.LoginDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserRegisterDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserSettingsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.UserMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.AuthenticationException;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    private final UserMapper userMapper;

    public AuthenticationEndpoint(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    /**
     * Logs in a user, using data from {@code userLoginDto}.
     *
     * @param userLoginDto login data from the client.
     * @return a ResponseEntity to send back to the client, containing the JWT authentication token.
     * @throws AuthenticationException if the login data could not be matched to a user or user could not be authenticated.
     * @author Marc Putz
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDto userLoginDto) throws AuthenticationException {
        LOGGER.trace("login({})", userLoginDto);

        try {
            // Look for user
            ApplicationUser user = userService.getUserByEmail(userLoginDto.getEmail());

            // Check if password exists
            if (userLoginDto.getPassword() == null) {
                throw new AuthenticationException("No password provided");
            }
            // encode password
            String encodedPassword = PasswordEncoder.encode(userLoginDto.getPassword(), userLoginDto.getEmail());

            // Check password data
            if (user.checkPasswordMatch(encodedPassword)) {

                // Create jwt token
                String authToken = AuthTokenUtils.createToken(user);

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
        try {
            userService.create(registerDto);
        } catch (ValidationException e) {
            throw new AuthenticationException(e.summary());
        }
        return login(loginDto);
    }

    /**
     * Updates user profile data.
     * Requires a valid JWT in the request header for authentication.
     *
     * @param userUpdateDto DTO containing updated user information.
     * @param headers       HTTP headers from the request, containing the JWT token.
     * @return ResponseEntity indicating the outcome of the operation.
     */
    @PutMapping("/settings")
    public ResponseEntity<UserSettingsDto> updateSettings(@RequestBody UserUpdateDto userUpdateDto,
                                                          @RequestHeader HttpHeaders headers) {
        LOGGER.trace("update({})", userUpdateDto);
        try {
            // retrieve token from authorization header
            String authToken = headers.getFirst("authorization");

            if (authToken == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            Long currentUserId = SessionManager.getInstance().getUserFromAuthToken(authToken);
            if (currentUserId == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            // Perform the update operation for the authenticated user
            ApplicationUser updatedUser = userService.update(userUpdateDto, currentUserId);
            UserSettingsDto userSettingsDto = userMapper.toUserSettingsDto(updatedUser);

            return ResponseEntity.ok(userSettingsDto);

        } catch (Exception e) {
            // Handle exceptions, such as authorization failure or other errors
            LOGGER.error("Error in update: ", e);
            //TODO Correct Exception Handling
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error during update: " + e.getMessage());
        }
    }

    @GetMapping("/settings")
    public ResponseEntity<UserSettingsDto> getSettings(@RequestHeader HttpHeaders headers) {
        LOGGER.trace("getSettings()");
        try {
            // retrieve token from authorization header
            String authToken = headers.getFirst("authorization");

            if (authToken == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            Long currentUserId = SessionManager.getInstance().getUserFromAuthToken(authToken);
            if (currentUserId == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            // Fetch user details and convert to DTO
            ApplicationUser currentUser = userService.getUserById(currentUserId);
            UserSettingsDto userSettingsDto = userMapper.toUserSettingsDto(currentUser);

            return ResponseEntity.ok(userSettingsDto);

        } catch (Exception e) {
            // Handle exceptions, such as user not found, authorization failure, or other errors
            LOGGER.error("Error in getSettings: ", e);
            //TODO Correct Exception Handling
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving user settings: " + e.getMessage());
        }
    }

    /**
     * Logs out a user, using the JWT authentication token in the HTTP header
     *
     * @param headers header of the HTTP request
     * @return a ResponseEntity to send back to the client, containing a boolean value indicating if the logout operation was successful.
     * @author Marc Putz
     */
    @PostMapping("/logout")
    public ResponseEntity<Boolean> logout(@RequestHeader HttpHeaders headers) {

        // retrieve token from authorization header
        String authToken = headers.getFirst("authorization");
        if (authToken == null) {
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        }

        // check token validity
        if (!AuthTokenUtils.isValid(authToken)) {
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        }

        // stop user session
        if (!SessionManager.getInstance().stopUserSession(authToken)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to stop user session");
        }

        return new ResponseEntity<>(true, HttpStatus.OK);
    }
}
