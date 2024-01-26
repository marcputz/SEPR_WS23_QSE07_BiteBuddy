package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeRatingDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeRatingListsDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthenticationService;
import at.ac.tuwien.sepr.groupphase.backend.service.ProfileService;
import at.ac.tuwien.sepr.groupphase.backend.utils.AuthTokenUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.lang.invoke.MethodHandles;

@RestController
@RequestMapping(path = ProfileEndpoint.BASE_PATH)
public class ProfileEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    static final String BASE_PATH = "/api/v1/profiles";

    private final ProfileService profileService;
    private AuthenticationService authenticationService;

    public ProfileEndpoint(ProfileService profileService, AuthenticationService authenticationService) {
        this.profileService = profileService;
        this.authenticationService = authenticationService;
    }


    @PostMapping("/search")
    public ProfileSearchResultDto searchProfiles(@Valid @RequestBody ProfileSearchDto searchParams, @RequestHeader HttpHeaders headers) {
        LOGGER.info("Received POST request for profile search on {}", BASE_PATH);
        LOGGER.debug("Search parameters: {}", searchParams);

        String authToken = this.authenticationService.getAuthToken(headers);
        Long currentUserId = AuthTokenUtils.getUserId(authToken);

        return profileService.searchProfiles(searchParams, currentUserId);
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
        LOGGER.info("Received PUT request on {}", BASE_PATH);
        LOGGER.debug("Request body for PUT:\n{}", recipeRatingDto);

        profileService.rateRecipe(recipeRatingDto);
    }



    @GetMapping("/{profileId}")
    public ProfileDetailDto getProfileDetails(@PathVariable long profileId) throws NotFoundException {
        LOGGER.info("Received Get request on {}", BASE_PATH);
        LOGGER.debug("Request body for Get:\n{}", profileId);

        return profileService.getProfileDetails(profileId);
    }

    @PutMapping("/edit/{id}")
    public ProfileDto editProfile(@RequestBody ProfileDto profileDto) throws ValidationException, NotFoundException {
        LOGGER.info("Received PUT request on {}", BASE_PATH);
        LOGGER.debug("Request body for PUT:\n{}", profileDto);

        return profileService.editProfile(profileDto);
    }

    @GetMapping("/userRating/{userId}")
    public RecipeRatingListsDto getRatingLists(@PathVariable long userId) throws NotFoundException {
        LOGGER.info("Received Get request on {}", BASE_PATH);
        LOGGER.debug("Request body for Get:\n{}", userId);

        return profileService.getRatingLists(userId);
    }

    @DeleteMapping("/{profileId}/delete")
    public void deleteProfile(@PathVariable long profileId) throws NotFoundException, ConflictException {
        LOGGER.info("Received Delete request on {}", BASE_PATH);
        LOGGER.debug("Request body for DELETE :\n{}", profileId);

        profileService.deleteProfile(profileId);
    }
}
