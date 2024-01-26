package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeDetailsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Allergene;
import at.ac.tuwien.sepr.groupphase.backend.entity.Ingredient;
import at.ac.tuwien.sepr.groupphase.backend.entity.Recipe;
import at.ac.tuwien.sepr.groupphase.backend.entity.RecipeIngredient;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;

import java.util.List;
import java.util.Set;

public interface RecipeService {
    /**
     * Finds all recipes that match the search parameters given.
     *
     * @param searchParams {@link RecipeSearchDto} with all parameters.
     * @return {@link RecipeSearchResultDto} with all the recipe entries that match the given search parameters and page information
     */
    RecipeSearchResultDto searchRecipes(RecipeSearchDto searchParams);

    /**
     * Gets all recipes in the data store.
     *
     * @return a list of recipes
     * @author Marc Putz
     */
    List<Recipe> getAll();

    /**
     * Gets all recipes in the data store which do not contain any allergens listed in the parameter.
     *
     * @param allergens a list of allergens which to filter recipes by.
     * @return a list of recipes.
     * @author Marc Putz
     */
    List<Recipe> getAllWithoutAllergens(Set<Allergene> allergens);

    /**
     * Gets all recipes from the data store which uses one or more ingredients from the given parameter and which do not contain any allergens listen in the second parameter list.
     *
     * @param ingredientNames a list of ingredient names to search recipes by.
     * @param allergens       a list of allergens which to filter recipes by.
     * @return a list of recipes.
     * @author Marc Putz
     */
    List<Recipe> getAllWithIngredientsWithoutAllergens(Set<String> ingredientNames, Set<Allergene> allergens);

    /**
     * Creates a recipe from all the details of the {@link RecipeDetailsDto}.
     * Before creating, it checks that every ingredient already exists in the database. If one does not an exception is thrown.
     *
     * @param recipe we want to store.
     */
    void createRecipe(RecipeDetailsDto recipe) throws ConflictException, ValidationException;

    /**
     * Gets the highest ID of any recipe in the data store.
     *
     * @return maximum ID of the recipe table.
     * @author Marc Putz
     */
    long getHighestRecipeId();

    /**
     * Gets the lowest ID of any recipe in the data store.
     *
     * @return minimum ID of the recipe table.
     * @author Marc Putz
     */
    long getLowestRecipeId();

    /**
     * Find a recipe by its ID.
     *
     * @param id the ID of the recipe to find.
     * @return the recipe entity
     * @throws NotFoundException if the recipe with the given ID does not exist in the data store.
     * @author Marc Putz
     */
    Recipe getRecipeById(long id) throws NotFoundException;

    /**
     * Finds the recipe with the given id.
     *
     * @param id is the id of the searched for recipe.
     * @return the {@link RecipeDto} with the given id
     * @throws NotFoundException if the recipe with the given ID does not exist in the persistent data store
     */
    RecipeDetailsDto getDetailedRecipe(long id) throws NotFoundException;

    /**
     * Finds the first 10 ingredients matching a name. It is not positional, upper/lower case-sensitive.
     * This finds basic and detailed ingredients.
     *
     * @param name which needs to be a substring of the ingredient.
     * @return list of matching ingredients.
     */
    List<String> findMatchingIngredients(String name);

    /**
     * Finds all RecipeIngredients which match the name of the detailed ingredient
     * @param name detailed ingredients name
     * @return List of RecipeIngredients
     */
    List<RecipeIngredient> findMatchingRecipeIngredients(String name);

    /**
     * Creates a Rating, based on the int rating for the recipe with id recipeID from the user with the id userID.
     *
     * @param recipeId is the id of the rated recipe.
     * @param userId   is the id of the rating user.
     * @param rating   is the id of the rating based on the int value (0 is dislike and 1 is like).
     * @throws NotFoundException if the recipe or the user can not be found.
     */
    void createRating(long recipeId, long userId, int rating);
}
