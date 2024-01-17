package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeRatingDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import jakarta.validation.Valid;

public interface ProfileService {
    /**
     * Save a single profile entry.
     *
     * @param profileDto to save
     * @return saved profile entry
     * @throws ValidationException if the profileDto is not valid
     */
    ProfileDto saveProfile(ProfileDto profileDto) throws ValidationException;

    /**
     * Likes or Dislikes a Recipe with a given Profile
     *
     * @param recipeRatingDto contains the profile id of the rating profile, recipe id of the recipe that needs to be rated and its rating (1 like, 0 dislike)
     * @throws ValidationException if the rating int is not 0 or 1
     * @throws NotFoundException if the profile or the recipe could not be found
     */
    void rateRecipe(RecipeRatingDto recipeRatingDto) throws NotFoundException, ValidationException;
}
