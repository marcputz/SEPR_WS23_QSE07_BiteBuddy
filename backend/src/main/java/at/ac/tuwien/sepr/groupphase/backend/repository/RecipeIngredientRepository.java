package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Recipe;
import at.ac.tuwien.sepr.groupphase.backend.entity.RecipeIngredient;
import at.ac.tuwien.sepr.groupphase.backend.entity.RecipeIngredientDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {
    /**
     * Finds all recipe ingredients which are used in a specific recipe.
     *
     * @param recipe for which want the recipe ingredients.
     * @return List of {@link RecipeIngredient} which are used in given recipe.
     * @author Thomas Hellweger
     */
    List<RecipeIngredient> findByRecipe(Recipe recipe);

    /**
     * Finds all recipe ingredients for a given (basic) ingredient.
     *
     * @param id of basic ingredient.
     * @return List of {@link RecipeIngredient} which use the basic ingredient.
     * @author Frederik Skiera
     */
    List<RecipeIngredient> findByIngredient_Id(Long id);

    /**
     * Finds all the recipes which use a specific {@link RecipeIngredientDetails}.
     *
     * @param details we want to find in the recipe ingredients.
     * @return List of {@link RecipeIngredient} which use the given recipe ingredient details.
     * @author Frederik Skiera
     */
    List<RecipeIngredient> findByAmountEquals(RecipeIngredientDetails details);
}
