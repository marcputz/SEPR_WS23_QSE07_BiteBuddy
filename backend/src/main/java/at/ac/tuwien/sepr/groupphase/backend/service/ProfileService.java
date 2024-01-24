package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeRatingDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeRatingListsDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;

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
     */
    ProfileDto saveProfile(ProfileDto profileDto) throws ValidationException;

    /**
     * Likes or Dislikes a Recipe with a given Profile.
     *
     * @param recipeRatingDto contains the profile id of the rating profile, recipe id of the recipe that needs to be rated and its rating (1 like, 0 dislike)
     * @throws ValidationException if the rating int is not 0 or 1
     * @throws NotFoundException   if the profile or the recipe could not be found
     */
    void rateRecipe(RecipeRatingDto recipeRatingDto) throws NotFoundException, ValidationException;

    /**
     * Likes or Dislikes a Recipe with a given Profile.
     *
     * @param id contains the current user's id
     * @return 2 lists of recipes, one containing liked and the other one disliked ones
     * @throws NotFoundException if the user could not be found
     */
    RecipeRatingListsDto getRatingLists(long id) throws NotFoundException;
}
