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
    private final AuthenticationService authenticationService;

    private final UserService userService;

    public ProfileEndpoint(ProfileService profileService, UserService userService, AuthenticationService authenticationService) {
        this.profileService = profileService;
        this.authenticationService = authenticationService;
        this.userService = userService;
    }

    /**
     * Searches all profiles who match the search parameters
     *
     * @param searchParams {@link ProfileSearchDto} contains the search information for the profile search
     * @param headers {@link HttpHeaders} with the authentication information.
     * @throws AuthenticationException if no user is logged in.
     * @author Alexander Pollek
     */
    @PostMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public ProfileSearchResultDto searchProfiles(@Valid @RequestBody ProfileSearchDto searchParams, @RequestHeader HttpHeaders headers)
        throws AuthenticationException {
        LOGGER.info("Received POST request on {}", BASE_PATH + "/search");
        LOGGER.debug("Request body for POST:\n{}", searchParams);
        this.authenticationService.verifyAuthenticated(headers);
        String authToken = this.authenticationService.getAuthToken(headers);
        Long currentUserId = AuthTokenUtils.getUserId(authToken);

        return profileService.searchProfiles(searchParams, currentUserId);
    }

    /**
     * Creates a profile.
     *
     * @param toCreateProfile contains the id and the creation information of the profile.
     * @param headers {@link HttpHeaders} with the authentication information.
     * @throws AuthenticationException if no user is logged in.
     * @throws NotFoundException       if the profile, it's user, one of its allergens or one of its liked ingredients do not exist in the database.
     * @throws ValidationException     if the edited data is not valid for editing.
     * @author Thomas Hellweger
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileDto createProfile(@Valid @RequestBody ProfileDto toCreateProfile, @RequestHeader HttpHeaders headers)
        throws ValidationException, AuthenticationException {
        LOGGER.info("Received POST request on {}", BASE_PATH);
        LOGGER.debug("Request body for POST:\n{}", toCreateProfile);

        this.authenticationService.verifyAuthenticated(headers);
        return profileService.saveProfile(toCreateProfile);

    }

    /**
     * Searches all profiles who match the search parameters
     *
     * @param profileId {@link ProfileSearchDto} contains the id of the profile to copy.
     * @param headers {@link HttpHeaders} with the authentication information.
     * @throws AuthenticationException if no user is logged in.
     * @author Alexander Pollek
     */
    @PostMapping("/copyToOwn/{profileId}")
    @ResponseStatus(HttpStatus.OK)
    public ProfileDetailDto copyProfile(@PathVariable Long profileId, @RequestHeader HttpHeaders headers) throws AuthenticationException {
        LOGGER.info("Received POST request on {}", BASE_PATH + "/copyToOwn");
        LOGGER.debug("Request body for POST:\n{}", profileId);
        this.authenticationService.verifyAuthenticated(headers);
        String authToken = this.authenticationService.getAuthToken(headers);
        Long currentUserId = AuthTokenUtils.getUserId(authToken);
        return profileService.copyToUser(profileId, currentUserId);
    }

    /**
     * Returns all profiles of a certain user.
     *
     * @param headers {@link HttpHeaders} with the authentication information.
     * @return {@link List<ProfileListDto>} of the found profiles.
     * @throws AuthenticationException if no user is logged in.
     * @author Marc Putz
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
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

    /**
     * Gets the details of an existing profile.
     *
     * @param profileId the id of the requested profile
     * @param headers {@link HttpHeaders} with the authentication information.
     * @return {@link ProfileDetailDto} of the requested profile.
     * @throws AuthenticationException if no user is logged in.
     * @throws NotFoundException if the profile does not exist in the database.
     * @author Thomas Hellweger
     */
    @GetMapping("/{profileId}")
    @ResponseStatus(HttpStatus.OK)
    public ProfileDetailDto getProfileDetails(@PathVariable long profileId, @RequestHeader HttpHeaders headers)
        throws NotFoundException, AuthenticationException {
        LOGGER.info("Received Get request on {}", BASE_PATH);
        LOGGER.debug("Request body for Get:\n{}", profileId);
        this.authenticationService.verifyAuthenticated(headers);
        return profileService.getProfileDetails(profileId);
    }

    /**
     * Edits an existing profile.
     *
     * @param profileDto contains the id and the edited information of the profile
     * @param headers {@link HttpHeaders} with the authentication information.
     * @return {@link ProfileDto} of the edited profile.
     * @throws AuthenticationException if no user is logged in.
     * @throws NotFoundException       if the profile or it's user do not exist in the database.
     * @throws ValidationException     if the edited data is not valid for editing.
     * @author Thomas Hellweger
     */
    @PutMapping("/edit/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProfileDto editProfile(@RequestBody ProfileDto profileDto, @RequestHeader HttpHeaders headers)
        throws ValidationException, NotFoundException, AuthenticationException {
        LOGGER.info("Received PUT request on {}", BASE_PATH);
        LOGGER.debug("Request body for PUT:\n{}", profileDto);
        this.authenticationService.verifyAuthenticated(headers);
        return profileService.editProfile(profileDto);
    }

    @PostMapping("/setActive/{profileId}")
    @ResponseStatus(HttpStatus.OK)
    public void setActiveProfile(@PathVariable Long profileId, @RequestHeader HttpHeaders headers)
        throws AuthenticationException, ConflictException {
        LOGGER.info("Received POST request to set active profile with ID {}", profileId);
        this.authenticationService.verifyAuthenticated(headers);
        String authToken = this.authenticationService.getAuthToken(headers);
        Long currentUserId = AuthTokenUtils.getUserId(authToken);

        profileService.setActiveProfile(profileId, currentUserId);
    }

    /**
     * Lets a profile rate a recipe.
     *
     * @param recipeRatingDto contains the id of the recipe to rate, the current user id (active profile is the one rating) and the rating (0-dislike, 1-like)
     * @param headers {@link HttpHeaders} with the authentication information.
     * @throws AuthenticationException if no user is logged in.
     * @throws NotFoundException if the user or the recipe do not exist in the database and if the user does not have an active profile.
     * @throws ValidationException if the rating value is not 0 or 1.
     * @author Thomas Hellweger
     */
    @PutMapping("/rating/{RecipeId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void putRating(@Valid @RequestBody RecipeRatingDto recipeRatingDto, @RequestHeader HttpHeaders headers)
        throws ValidationException, NotFoundException, AuthenticationException {
        LOGGER.info("Received PUT request on {}", BASE_PATH);
        LOGGER.debug("Request body for PUT:\n{}", recipeRatingDto);

        this.authenticationService.verifyAuthenticated(headers);
        profileService.rateRecipe(recipeRatingDto);
    }

    /**
     * Gets a list of the liked and disliked recipes of a profile.
     *
     * @param userId the id of the profile which wants the liked and disliked lists
     * @param headers {@link HttpHeaders} with the authentication information.
     * @return returns a list of the liked- and a list of the disliked recipes.
     * @throws AuthenticationException if no user is logged in.
     * @throws NotFoundException       if the profile containing the lists does not exist.
     * @author Thomas Hellweger
     */
    @GetMapping("/rating/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public RecipeRatingListsDto getRatingLists(@PathVariable long userId, @RequestHeader HttpHeaders headers)
        throws NotFoundException, AuthenticationException {
        LOGGER.info("Received Get request on {}", BASE_PATH);
        LOGGER.debug("Request body for Get:\n{}", userId);
        this.authenticationService.verifyAuthenticated(headers);
        return profileService.getRatingLists(userId);
    }

    /**
     * Deletes an existing profile.
     *
     * @param profileId id of profile to delete.
     * @param headers {@link HttpHeaders} with the authentication information.
     * @return returns a ProfileDto which contains the delete profile's data.
     * @throws AuthenticationException if no user is logged in.
     * @throws NotFoundException       if the profile to delete does not exist in the database.
     * @throws ConflictException       if the user wants to delete the active profile or the current profile does not belong to the current user.
     * @author Thomas Hellweger
     */
    @DeleteMapping("/deleteProfile/{profileId}")
    @ResponseStatus(HttpStatus.OK)
    public ProfileDto delete(@PathVariable long profileId, @RequestHeader HttpHeaders headers)
        throws NotFoundException, AuthenticationException, ConflictException {
        LOGGER.info("Received Delete request on {}", BASE_PATH);
        LOGGER.debug("Request body for Delete:\n{}", profileId);

        this.authenticationService.verifyAuthenticated(headers);
        String authToken = this.authenticationService.getAuthToken(headers);
        Long currentUserId = AuthTokenUtils.getUserId(authToken);
        return profileService.deleteProfile(profileId, currentUserId);
    }
}
