package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    /**
     * Finds ingredients which are used in a recipe with a specific index.
     *
     * @param l recipe id we want the ingredients for.
     * @return Set of {@link Ingredient} which all are used in a specific recipe.
     * @author Anton Nather
     */
    Set<Ingredient> findAllByRecipeIngredientsRecipeId(long l);

    /**
     * Finds all ingredients which are mapped to the give allergen id.
     *
     * @param l allergen id.
     * @return Set of {@link Ingredient} which all are contained in the given allergen.
     * @author Anton Nather
     */
    Set<Ingredient> findAllByAllergeneIngredientsAllergeneId(long l);

    /**
     * Finds all ingredients matching given name. It is not case-sensitive.
     *
     * @param name for which we want to filter ingredients by.
     * @return List of {@link Ingredient} which match given name.
     * @author Frederik Skiera
     */
    List<Ingredient> findByNameContainingIgnoreCase(String name);
}
