package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.auth.AuthTokenUtils;
import at.ac.tuwien.sepr.groupphase.backend.auth.SessionManager;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.LoginDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.lang.invoke.MethodHandles;

@RestController
@RequestMapping(value = "/api/v1/authentication")
public class LoginEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserService userService;

    public LoginEndpoint(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDto userLoginDto) {
        LOGGER.trace("login({})", userLoginDto);

        try {
            // Look for user
            ApplicationUser user = userService.getUserByEmail(userLoginDto.getEmail());

            // Check password data
            if (user.checkPasswordMatch(userLoginDto.getPasswordEncoded())) {

                // Create jwt token
                String authToken = AuthTokenUtils.createToken(user.getId(), user.getNickname());

                // Register user session
                SessionManager.startUserSession(user.getId(), authToken);

                // Return jwt auth token
                return new ResponseEntity<String>(authToken, HttpStatus.OK);

            } else {
                // wrong password
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Password not valid");
            }
        } catch (UserNotFoundException e) {
            // login email not found
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with email '" + userLoginDto.getEmail() + "' does not exist", e);
        }
    }
}
