package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeDetailsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
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
     * Creates a recipe from all the details of the {@link RecipeDetailsDto}.
     * Before creating, it checks that every ingredient already exists in the database. If one does not an exception is thrown.
     *
     * @param recipe we want to store.
     */
    void createRecipe(RecipeDetailsDto recipe) throws ConflictException;

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
     *
     * @param name which needs to be a substring of the ingredient.
     * @return list of matching ingredients.
     */
    List<String> findMatchingIngredients(String name);

    /**
     * Creates a Raiting, based on the int raiting for the recipe with id recipeID from the user with the id userID
     *
     * @param recipeId is the id of the rated recipe.
     * @param userId is the id of the rating user.
     * @param rating is the id of the raiting based on the int value (0 is dislike and 1 is like).
     * @throws NotFoundException if the recipe or the user can not be found.
     */
    void createRating(long recipeId, long userId, int rating);
}
