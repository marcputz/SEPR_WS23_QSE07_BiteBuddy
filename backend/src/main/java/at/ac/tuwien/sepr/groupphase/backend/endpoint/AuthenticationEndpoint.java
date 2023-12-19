package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.utils.AuthTokenUtils;
import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.LoginDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ResetPasswordDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserRegisterDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserSettingsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserUpdateEmailAndPasswordDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.UserMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.AuthenticationException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthenticationService;
import at.ac.tuwien.sepr.groupphase.backend.service.PasswordResetService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.lang.invoke.MethodHandles;

/**
 * REST endpoint for user authentication.
 */
@RestController
@RequestMapping(value = "/api/v1/authentication")
public class AuthenticationEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserService userService;

    private final AuthenticationService authenticationService;

    private final UserMapper userMapper;

    private final PasswordResetService passwordResetService;

    public AuthenticationEndpoint(UserService userService, AuthenticationService authService, PasswordResetService passwordResetService,
                                  UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.passwordResetService = passwordResetService;
        this.authenticationService = authService;
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

        String authToken = authenticationService.loginUser(userLoginDto);

        return new ResponseEntity<>(authToken, HttpStatus.OK);
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
            return login(loginDto);
        } catch (ValidationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Updates the email and/or password of a user's profile.
     * Requires a valid JWT in the request header for authentication. The current password of the user is verified before
     * updating the email and/or password. The method allows updating either the email, password, or both.
     *
     * @param userUpdateEmailAndPasswordDto DTO containing the new email and/or password along with the current password for verification.
     * @param headers                       HTTP headers from the request, containing the JWT token.
     * @return a ResponseEntity indicating the outcome of the operation. The updated user settings are returned in the response body.
     * @throws AuthenticationException if the authentication fails due to invalid token or incorrect current password.
     * @throws ValidationException     if the provided new email or password fails validation checks.
     * @throws ConflictException       if the new email conflicts with another user's email.
     * @throws UserNotFoundException   if no user is found with the provided ID in the JWT token.
     */
    @PutMapping("/settings")
    public ResponseEntity<UserSettingsDto> updateEmailAndPasswordSettings(@RequestBody UserUpdateEmailAndPasswordDto userUpdateEmailAndPasswordDto,
                                                                          @RequestHeader HttpHeaders headers)
        throws AuthenticationException, ValidationException, ConflictException, UserNotFoundException {
        LOGGER.trace("update({})", userUpdateEmailAndPasswordDto);

        this.authenticationService.verifyAuthenticated(headers);
        // Retrieve token from authorization header
        String authToken = headers.getFirst("Authorization");
        Long currentUserId = AuthTokenUtils.getUserId(authToken);

        if (currentUserId == null) {
            LOGGER.warn("Update user did not find ID in token");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        // Check if the current password is correct
        authenticationService.verifyUserPassword(currentUserId, userUpdateEmailAndPasswordDto.getCurrentPassword());

        // Update the authenticated user
        ApplicationUser updatedUser = userService.updateEmailAndPassword(userUpdateEmailAndPasswordDto, currentUserId);
        UserSettingsDto userSettingsDto = userMapper.toUserSettingsDto(updatedUser);

        return ResponseEntity.ok(userSettingsDto);
    }

    /**
     * Retrieves the settings of the currently authenticated user.
     * The method requires a valid JWT in the request header for authentication.
     * It fetches the user details based on the user ID extracted from the JWT and returns them as a UserSettingsDto.
     *
     * @param headers HTTP headers from the request, containing the JWT token.
     * @return a ResponseEntity containing the UserSettingsDto of the authenticated user.
     * @throws AuthenticationException if the user is not authenticated or the authentication token is invalid.
     * @throws UserNotFoundException   if the user corresponding to the ID in the JWT token is not found.
     */
    @GetMapping("/settings")
    public ResponseEntity<UserSettingsDto> getSettings(@RequestHeader HttpHeaders headers) throws AuthenticationException {
        LOGGER.trace("getSettings()");

        authenticationService.verifyAuthenticated(headers);
        try {
            // Retrieve token from authorization header
            String authToken = this.authenticationService.getAuthToken(headers);
            Long currentUserId = AuthTokenUtils.getUserId(authToken);
            ApplicationUser currentUser = userService.getUserById(currentUserId);
            UserSettingsDto userSettingsDto = userMapper.toUserSettingsDto(currentUser);
            return ResponseEntity.ok(userSettingsDto);
        } catch (UserNotFoundException e) {
            LOGGER.warn("User not found: ", e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found", e);
        }
    }

    /**
     * Logs out a user, using the JWT authentication token in the HTTP header.
     *
     * @param headers header of the HTTP request
     * @return a ResponseEntity to send back to the client, containing a boolean value indicating if the logout operation was successful.
     * @author Marc Putz
     */
    @PostMapping("/logout")
    public ResponseEntity<Boolean> logout(@RequestHeader HttpHeaders headers) throws AuthenticationException {

        this.authenticationService.verifyAuthenticated(headers);

        // retrieve token from authorization header
        String authToken = this.authenticationService.getAuthToken(headers);
        authenticationService.logoutUser(authToken);

        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    /**
     * Requests a password reset for a certain user, identified by their email.
     *
     * @param requestBody a json body containing the user's email address
     * @return a ResponseEntity to send back to the client, containing a boolean value indicating if the request was successful.
     * @author Marc Putz
     */
    @PostMapping("/request_password_reset")
    public ResponseEntity<Boolean> requestPasswordReset(@RequestBody String requestBody) {
        LOGGER.trace("requestPasswordReset({})", requestBody);

        // check if request contains email
        Object requestEmail = JsonParserFactory.getJsonParser().parseMap(requestBody).get("email");
        if (requestEmail instanceof String email) {

            try {
                passwordResetService.requestPasswordReset(email);
                return new ResponseEntity<>(true, HttpStatus.OK);

            } catch (UserNotFoundException ex) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User '" + email + "' does not exist");

            } catch (MessagingException ex) {
                // this occurs when the server cannot send an email
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Can not send a reset email at this current time. Try again later");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request does not contain email");
        }

    }

    /**
     * Resets the password of a user. Needs correct authorization given in the DTO object
     *
     * @param dto the reset dto containing valid identification and data
     * @return a ResponseEntity to send back to the client, containing a boolean value indicating if the reset was successful.
     * @throws ValidationException if the new password does not match the validation requirements
     * @author Marc Putz
     */
    @PostMapping("/password_reset")
    public ResponseEntity<Boolean> resetPassword(@RequestBody ResetPasswordDto dto) throws ValidationException, AuthenticationException {
        LOGGER.trace("resetPassword({})", dto);

        passwordResetService.resetPassword(dto);

        return new ResponseEntity<>(true, HttpStatus.OK);
    }
}
