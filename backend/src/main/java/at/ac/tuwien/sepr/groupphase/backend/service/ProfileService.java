package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeRatingDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeRatingListsDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Profile;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;

import java.util.List;


public interface ProfileService {
    /**
     * Save a single profile entry.
     *
     * @param profileDto to save
     * @return saved profile entry
     * @throws ValidationException if the profileDto is not valid
     */
    ProfileDto saveProfile(ProfileDto profileDto) throws ValidationException;

    List<ProfileListDto> getAllByUser(ApplicationUser user);

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
