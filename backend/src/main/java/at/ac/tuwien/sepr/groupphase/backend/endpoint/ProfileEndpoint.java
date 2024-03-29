package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeRatingDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeRatingListsDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.AuthenticationException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthenticationService;
import at.ac.tuwien.sepr.groupphase.backend.service.ProfileService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.utils.AuthTokenUtils;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    private AuthenticationService authenticationService;

    private final UserService userService;

    public ProfileEndpoint(ProfileService profileService, UserService userService, AuthenticationService authenticationService) {
        this.profileService = profileService;
        this.authenticationService = authenticationService;
        this.userService = userService;
    }


    @PostMapping("/search")
    public ProfileSearchResultDto searchProfiles(@Valid @RequestBody ProfileSearchDto searchParams, @RequestHeader HttpHeaders headers)
        throws AuthenticationException {
        LOGGER.info("Received POST request on {}", BASE_PATH + "/search");
        LOGGER.debug("Request body for POST:\n{}", searchParams);
        this.authenticationService.verifyAuthenticated(headers);
        String authToken = this.authenticationService.getAuthToken(headers);
        Long currentUserId = AuthTokenUtils.getUserId(authToken);

        return profileService.searchProfiles(searchParams, currentUserId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileDto createProfile(@Valid @RequestBody ProfileDto toCreateProfile, @RequestHeader HttpHeaders headers)
        throws ValidationException, AuthenticationException {
        LOGGER.info("Received POST request on {}", BASE_PATH);
        LOGGER.debug("Request body for POST:\n{}", toCreateProfile);

        this.authenticationService.verifyAuthenticated(headers);
        return profileService.saveProfile(toCreateProfile);
    }


    @PostMapping("/copyToOwn/{profileId}")
    public ProfileDetailDto post(@PathVariable Long profileId, @RequestHeader HttpHeaders headers) throws AuthenticationException {
        LOGGER.info("Received POST request on {}", BASE_PATH + "/copyToOwn");
        LOGGER.debug("Request body for POST:\n{}", profileId);
        this.authenticationService.verifyAuthenticated(headers);
        String authToken = this.authenticationService.getAuthToken(headers);
        Long currentUserId = AuthTokenUtils.getUserId(authToken);
        return profileService.copyToUser(profileId, currentUserId);
    }

    @GetMapping
    public List<ProfileListDto> getAllForUser(@RequestHeader HttpHeaders headers) throws AuthenticationException {
        LOGGER.trace("getAllForUser({})", headers);

        this.authenticationService.verifyAuthenticated(headers);

        try {
            Long userId = AuthTokenUtils.getUserId(this.authenticationService.getAuthToken(headers));
            ApplicationUser thisUser = this.userService.getUserById(userId);

            return this.profileService.getAllByUser(thisUser);

        } catch (NotFoundException ex) {
            throw new AuthenticationException("Error retrieving user data", ex);
        }
    }

    @GetMapping("/{profileId}")
    public ProfileDetailDto getProfileDetails(@PathVariable long profileId, @RequestHeader HttpHeaders headers)
        throws NotFoundException, AuthenticationException {
        LOGGER.info("Received Get request on {}", BASE_PATH);
        LOGGER.debug("Request body for Get:\n{}", profileId);
        this.authenticationService.verifyAuthenticated(headers);
        return profileService.getProfileDetails(profileId);
    }

    @PutMapping("/edit/{id}")
    public ProfileDto editProfile(@RequestBody ProfileDto profileDto, @RequestHeader HttpHeaders headers)
        throws ValidationException, NotFoundException, AuthenticationException {
        LOGGER.info("Received PUT request on {}", BASE_PATH);
        LOGGER.debug("Request body for PUT:\n{}", profileDto);
        this.authenticationService.verifyAuthenticated(headers);
        return profileService.editProfile(profileDto);
    }

    @PostMapping("/setActive/{profileId}")
    public void setActiveProfile(@PathVariable Long profileId, @RequestHeader HttpHeaders headers)
        throws AuthenticationException, ConflictException {
        LOGGER.info("Received POST request to set active profile with ID {}", profileId);
        this.authenticationService.verifyAuthenticated(headers);
        String authToken = this.authenticationService.getAuthToken(headers);
        Long currentUserId = AuthTokenUtils.getUserId(authToken);

        profileService.setActiveProfile(profileId, currentUserId);
    }

    @PutMapping("/rating/{RecipeId}")
    public void postRating(@Valid @RequestBody RecipeRatingDto recipeRatingDto, @RequestHeader HttpHeaders headers)
        throws ValidationException, NotFoundException, AuthenticationException {
        LOGGER.info("Received PUT request on {}", BASE_PATH);
        LOGGER.debug("Request body for PUT:\n{}", recipeRatingDto);
        this.authenticationService.verifyAuthenticated(headers);
        profileService.rateRecipe(recipeRatingDto);
    }

    @GetMapping("/rating/{userId}")
    public RecipeRatingListsDto getRatingLists(@PathVariable long userId, @RequestHeader HttpHeaders headers)
        throws NotFoundException, AuthenticationException {
        LOGGER.info("Received Get request on {}", BASE_PATH);
        LOGGER.debug("Request body for Get:\n{}", userId);
        this.authenticationService.verifyAuthenticated(headers);
        return profileService.getRatingLists(userId);
    }

    @DeleteMapping("/deleteProfile/{profileId}")
    public ProfileDto delete(@PathVariable long profileId, @RequestHeader HttpHeaders headers)
        throws NotFoundException, AuthenticationException, ConflictException {
        LOGGER.info("Received Delete request on {}", BASE_PATH);

        this.authenticationService.verifyAuthenticated(headers);
        String authToken = this.authenticationService.getAuthToken(headers);
        Long currentUserId = AuthTokenUtils.getUserId(authToken);
        return profileService.deleteProfile(profileId, currentUserId);
    }
}
