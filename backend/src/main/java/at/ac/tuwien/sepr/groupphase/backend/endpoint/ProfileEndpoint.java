package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeRatingDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeRatingListsDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.AuthenticationException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthenticationService;
import at.ac.tuwien.sepr.groupphase.backend.service.ProfileService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.utils.AuthTokenUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping(path = ProfileEndpoint.BASE_PATH)
public class ProfileEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    static final String BASE_PATH = "/api/v1/profiles";

    private final ProfileService profileService;

    private final UserService userService;

    private final AuthenticationService authService;

    public ProfileEndpoint(ProfileService profileService, UserService userService, AuthenticationService authService) {
        this.profileService = profileService;
        this.authService = authService;
        this.userService = userService;
    }

    @Operation(summary = "Create profile", description = "Profile fields should be valid")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "422", description = "Unable to process the data, because contains invalid data"),
        @ApiResponse(responseCode = "400", description = "Illegal arguments in the request body")})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileDto post(@Valid @RequestBody ProfileDto toCreateProfile) throws ValidationException {
        LOGGER.info("Received POST request on {}", BASE_PATH);
        LOGGER.debug("Request body for POST:\n{}", toCreateProfile);

        return profileService.saveProfile(toCreateProfile);
    }

    @PutMapping("/rating/{RecipeId}")
    public void post(@Valid @RequestBody RecipeRatingDto recipeRatingDto) throws ValidationException, NotFoundException {
        LOGGER.info("Received POST request on {}", BASE_PATH);
        LOGGER.debug("Request body for POST:\n{}", recipeRatingDto);

        profileService.rateRecipe(recipeRatingDto);
    }

    @GetMapping
    public List<ProfileListDto> getAllForUser(@RequestHeader HttpHeaders headers) throws AuthenticationException {
        LOGGER.trace("getAllForUser({})", headers);

        this.authService.verifyAuthenticated(headers);

        try {
            Long userId = AuthTokenUtils.getUserId(this.authService.getAuthToken(headers));
            ApplicationUser thisUser = this.userService.getUserById(userId);

            return this.profileService.getAllByUser(thisUser);

        } catch (NotFoundException ex) {
            throw new AuthenticationException("Error retrieving user data", ex);
        }
    }

    @GetMapping("/rating/{UserId}")
    public RecipeRatingListsDto get(@PathVariable long userId) throws NotFoundException {
        LOGGER.info("Received Get request on {}", BASE_PATH);
        LOGGER.debug("Request body for Get:\n{}", userId);

        return profileService.getRatingLists(userId);
    }


}
