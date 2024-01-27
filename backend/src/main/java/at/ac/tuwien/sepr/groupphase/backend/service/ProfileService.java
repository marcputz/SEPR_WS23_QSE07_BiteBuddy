package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeRatingDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeRatingListsDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Profile;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;

import java.util.List;


public interface ProfileService {
    /**
     * Searches for profiles based on criteria provided in {@link ProfileSearchDto} and the current user's ID.
     *
     * <p>This method returns a paginated list of profiles matching the search criteria, including profile name,
     * creator's nickname, and user ID. It supports filtering profiles based on whether the search is for the
     * user's own profiles or others'. When 'ownProfiles' in {@link ProfileSearchDto} is true, it fetches profiles
     * created by the current user specified by 'currentUserId'. Otherwise, it fetches profiles matching the name
     * and creator's nickname but excludes profiles associated with 'currentUserId'.
     *
     * <p>Default pagination values are applied if not specified in the search parameters.
     *
     * @param searchParams  The criteria for searching profiles including flags for own profile search.
     * @param currentUserId The ID of the current user, used to filter own profiles or exclude from search.
     * @return A paginated {@link ProfileSearchResultDto} containing matching profiles with detailed information and pagination details.
     */
    ProfileSearchResultDto searchProfiles(ProfileSearchDto searchParams, Long currentUserId);

    /**
     * Save a single profile entry.
     *
     * @param profileDto to save
     * @return saved profile entry
     * @throws ValidationException if the profileDto is not valid
     * @throws NotFoundException   if the given user id, at least one of the allergens or at least one of the ingredients can not be found in the database
     */
    ProfileDto saveProfile(ProfileDto profileDto) throws ValidationException, NotFoundException;

    List<ProfileListDto> getAllByUser(ApplicationUser user);

    Profile getById(long profileId) throws NotFoundException;

    /**
     * Creates a copy of an existing profile for a specified user.
     *
     * @param profileId The ID of the profile to be copied.
     * @param userId    The ID of the user for whom the profile will be copied.
     * @return A {@link ProfileDetailDto} object representing the newly created profile copy.
     * @throws NotFoundException if either the profile or the user specified by their IDs are not found in the database.
     */
    ProfileDetailDto copyToUser(Long profileId, Long userId);

    /**
     * Likes or Dislikes a Recipe with a given Profile.
     *
     * @param recipeRatingDto contains the profile id of the rating profile, recipe id of the recipe that needs to be rated and its rating (1 like, 0 dislike)
     * @throws ValidationException if the rating int is not 0 or 1
     * @throws NotFoundException   if the profile or the recipe could not be found
     */
    void rateRecipe(RecipeRatingDto recipeRatingDto) throws NotFoundException, ValidationException;

    /**
     * Returns the liked and disliked list of recipes of a profile.
     *
     * @param id contains the current user's id
     * @return 2 lists of recipes, one containing liked and the other one disliked ones
     * @throws NotFoundException if the user could not be found or the user's active Profile does not exist
     */
    RecipeRatingListsDto getRatingLists(long id) throws NotFoundException;

    /**
     * Returns a profile's detailed information.
     *
     * @param id contains the profile's id
     * @return the details of a profile (id, name, allergenes, ingredients, liked recipes, disliked recipes
     * @throws NotFoundException if the profile could not be found
     */
    ProfileDetailDto getProfileDetails(long id) throws NotFoundException;

    /**
     * Edits a single profile entry.
     *
     * @param profileDto to edit
     * @return edited profile entry
     * @throws ValidationException if the profileDto is not valid
     * @throws NotFoundException   if the profileDto's contents (profile, ingredients, allergenes, user) does not exist in the database
     */
    ProfileDto editProfile(ProfileDto profileDto) throws ValidationException, NotFoundException;

    /**
     * Deletes a single profile entry.
     *
     * @param profileId to edit
     * @throws NotFoundException if the profile does not exist in the database
     * @throws ConflictException if the user tries to delete the active profile
     */
    ProfileDto deleteProfile(Long profileId, Long userId) throws NotFoundException, ConflictException;

    /**
     * Sets the active profile for a user.
     *
     * <p>Ensures the profile and user exist and that the profile belongs to the user. If the profile
     * is already active for the user, no changes are made. Throws {@link NotFoundException} if the
     * profile or user is not found, and {@link ConflictException} if the profile does not belong to
     * the user.
     *
     * @param profileId The ID of the profile to be activated.
     * @param userId    The ID of the user.
     * @throws NotFoundException If the profile or user is not found.
     * @throws ConflictException If the profile does not belong to the user.
     */
    void setActiveProfile(Long profileId, Long userId) throws ConflictException;
}
