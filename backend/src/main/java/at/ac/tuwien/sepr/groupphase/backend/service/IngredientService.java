package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.IngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Ingredient;

import java.util.List;

/**
 * This class is used to provide functionality for ingredients.
 */
public interface IngredientService {

    /**
     * Returns a list of all ingredients in the database.
     *
     * @return all ingredients
     */
    List<IngredientDto> getAllIngredients();

    /**
     * Returns ingredient which matches given id.
     *
     * @param id of which we want the ingredient.
     * @return ingredient
     */
    Ingredient getById(long id);

    /**
     * Returns a list of all ingredients matching the given name. The search is not case-sensitive.
     *
     * @param name for which we to search ingredients.
     * @return List of ingredients matching searched name.
     */
    List<Ingredient> getByNameMatching(String name);

    /**
     * Returns a list of all ingredients matching the given name. The search is not case-sensitive.
     *
     * @param name for which we to search ingredients.
     * @return List of ingredients names matching searched name.
     */
    List<String> getNamesMatching(String name);
}
