package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.authentication.LoginDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.authentication.ResetPasswordDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserRegisterDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserSettingsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserUpdateEmailAndPasswordDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserUpdateSettingsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.UserMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.AuthenticationException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.DataStoreException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthenticationService;
import at.ac.tuwien.sepr.groupphase.backend.service.PasswordResetService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.utils.AuthTokenUtils;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.lang.invoke.MethodHandles;

/**
 * REST endpoint for users.
 */
@RestController
@RequestMapping(value = "/api/v1/user")
public class UserEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final PasswordResetService passwordResetService;

    private final UserMapper userMapper;

    public UserEndpoint(UserService userService, AuthenticationService authService, PasswordResetService passwordResetService,
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
    @ResponseStatus(HttpStatus.OK)
    public String login(@RequestBody LoginDto userLoginDto) throws AuthenticationException {
        LOGGER.trace("login({})", userLoginDto);

        return authenticationService.loginUser(userLoginDto);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public String register(@RequestBody UserRegisterDto registerDto) throws AuthenticationException, ConflictException, ValidationException {
        LOGGER.trace("register({})", registerDto);

        String encodedPassword = PasswordEncoder.encode(registerDto.getPasswordEncoded(), registerDto.getEmail());

        LoginDto loginDto = new LoginDto();
        loginDto.setPassword(registerDto.getPasswordEncoded());
        loginDto.setEmail(registerDto.getEmail());
        registerDto.setPasswordEncoded(encodedPassword);

        userService.create(registerDto);
        return this.login(loginDto);
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
     * @throws NotFoundException       if no user is found with the provided ID in the JWT token.
     */
    @PutMapping("/settings/authentication")
    @ResponseStatus(HttpStatus.OK)
    public UserSettingsDto updateEmailAndPasswordSettings(@RequestBody UserUpdateEmailAndPasswordDto userUpdateEmailAndPasswordDto,
                                                          @RequestHeader HttpHeaders headers)
        throws AuthenticationException, DataStoreException, ValidationException, ConflictException, NotFoundException {
        LOGGER.trace("updateEmailAndPasswordSettings({},{})", userUpdateEmailAndPasswordDto, headers);

        this.authenticationService.verifyAuthenticated(headers);

        // Retrieve user id
        Long currentUserId = AuthTokenUtils.getUserId(this.authenticationService.getAuthToken(headers));

        if (currentUserId == null) {
            // this should never happen
            throw new NotFoundException("User Identification not found");
        }

        // Check if the current password is correct
        authenticationService.verifyUserPassword(currentUserId, userUpdateEmailAndPasswordDto.getCurrentPassword());

        // Update the authenticated user
        ApplicationUser updatedUser = userService.updateEmailAndPassword(userUpdateEmailAndPasswordDto, currentUserId);
        return userMapper.toUserSettingsDto(updatedUser);
    }

    @PutMapping("/settings")
    @ResponseStatus(HttpStatus.OK)
    public UserSettingsDto updateUserSettings(@RequestBody UserUpdateSettingsDto userUpdateSettingsDto,
                                              @RequestHeader HttpHeaders headers)
        throws AuthenticationException, DataStoreException, ValidationException, ConflictException, NotFoundException {
        LOGGER.trace("updateUserSettings({},{})", userUpdateSettingsDto, headers);

        this.authenticationService.verifyAuthenticated(headers);

        // Retrieve user id
        Long currentUserId = AuthTokenUtils.getUserId(this.authenticationService.getAuthToken(headers));

        if (currentUserId == null) {
            // this should never happen
            throw new NotFoundException("User Identification not found");
        }

        // Update the authenticated user
        ApplicationUser updatedUser = userService.updateSettings(userUpdateSettingsDto, currentUserId);
        return userMapper.toUserSettingsDto(updatedUser);
    }

    /**
     * Retrieves the settings of the currently authenticated user.
     * The method requires a valid JWT in the request header for authentication.
     * It fetches the user details based on the user ID extracted from the JWT and returns them as a UserSettingsDto.
     *
     * @param headers HTTP headers from the request, containing the JWT token.
     * @return a ResponseEntity containing the UserSettingsDto of the authenticated user.
     * @throws AuthenticationException if the user is not authenticated or the authentication token is invalid.
     * @throws NotFoundException       if the user corresponding to the ID in the JWT token is not found.
     */
    @GetMapping("/settings")
    @ResponseStatus(HttpStatus.OK)
    public UserSettingsDto getSettings(@RequestHeader HttpHeaders headers) throws AuthenticationException {
        LOGGER.trace("getSettings({})", headers);

        authenticationService.verifyAuthenticated(headers);

        // Retrieve token from authorization header
        String authToken = this.authenticationService.getAuthToken(headers);
        Long currentUserId = AuthTokenUtils.getUserId(authToken);
        ApplicationUser currentUser = userService.getUserById(currentUserId);

        return userMapper.toUserSettingsDto(currentUser);
    }

    /**
     * Logs out a user, using the JWT authentication token in the HTTP header.
     *
     * @param headers header of the HTTP request
     * @author Marc Putz
     */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout(@RequestHeader HttpHeaders headers) throws AuthenticationException {

        this.authenticationService.verifyAuthenticated(headers);

        // retrieve token from authorization header
        String authToken = this.authenticationService.getAuthToken(headers);
        authenticationService.logoutUser(authToken);
    }

    /**
     * Requests a password reset for a certain user, identified by their email.
     *
     * @param requestBody a json body containing the user's email address
     * @throws MessagingException if something went wrong with sending the email message.
     * @author Marc Putz
     */
    @PostMapping("/request_password_reset")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void requestPasswordReset(@RequestBody String requestBody) throws MessagingException {
        LOGGER.trace("requestPasswordReset({})", requestBody);

        // check if request contains email
        Object requestEmail = JsonParserFactory.getJsonParser().parseMap(requestBody).get("email");
        if (requestEmail instanceof String email) {

            passwordResetService.requestPasswordReset(email);

        } else {
            throw new IllegalArgumentException("Invalid request body format");
        }
    }

    /**
     * Resets the password of a user. Needs correct authorization given in the DTO object
     *
     * @param dto the reset dto containing valid identification and data
     * @throws ValidationException if the new password does not match the validation requirements
     * @author Marc Putz
     */
    @PostMapping("/password_reset")
    @ResponseStatus(HttpStatus.OK)
    public void resetPassword(@RequestBody ResetPasswordDto dto) throws ValidationException, AuthenticationException {
        LOGGER.trace("resetPassword({})", dto);

        passwordResetService.resetPassword(dto);
    }
}
