package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeDetailsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;

import java.util.List;

public interface RecipeService {
    /**
     * Finds all recipes that match the search parameters given.
     *
     * @param searchParams {@link RecipeSearchDto} with all parameters.
     * @return List of {@link RecipeListDto} with all the recipe entries that match the given search parameters
     */
    List<RecipeListDto> searchRecipes(RecipeSearchDto searchParams);

    /**
     * Finds the recipe with the given id.
     *
     * @param id is the id of the searched for recipe.
     * @return the {@link RecipeDto} with the given id
     * @throws NotFoundException if the recipe with the given ID does not exist in the persistent data store
     */
    RecipeDetailsDto getDetailedRecipe(long id) throws NotFoundException;
}
