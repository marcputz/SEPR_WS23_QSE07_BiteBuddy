package at.ac.tuwien.sepr.groupphase.backend.repository;


import at.ac.tuwien.sepr.groupphase.backend.entity.Recipe;
import at.ac.tuwien.sepr.groupphase.backend.entity.RecipeIngredient;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;


@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    Page<Recipe> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Recipe> findByNameContainingIgnoreCase(String name);

    Set<Recipe> findAllByRecipeIngredientsIngredientId(long l);

    @Transactional
    default void updateIngredients(Long id, Set<RecipeIngredient> ingredients) {
        Recipe recipe = findById(id).orElse(null);
        if (recipe != null) {
            recipe.setIngredients(ingredients);
            save(recipe);
        }
    }
}
