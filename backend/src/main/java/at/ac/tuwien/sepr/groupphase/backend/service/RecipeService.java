package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeSearchDto;

import java.util.List;

public interface RecipeService {
    /**
     * Finds all recipes that match the search parameters given.
     *
     * @param searchParams {@link RecipeSearchDto} with all parameters.
     * @return List of {@link RecipeListDto} with all the recipe entries that match the given search parameters
     */
    List<RecipeListDto> searchRecipes(RecipeSearchDto searchParams);
}
