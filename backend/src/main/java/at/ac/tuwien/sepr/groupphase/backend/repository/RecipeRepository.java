package at.ac.tuwien.sepr.groupphase.backend.repository;


import at.ac.tuwien.sepr.groupphase.backend.entity.Recipe;
import at.ac.tuwien.sepr.groupphase.backend.entity.RecipeIngredient;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("select distinct ri.recipe from RecipeIngredient ri "
        + "left join Ingredient i on ri.ingredient = i "
        + "left join AllergeneIngredient ai on ai.ingredient = i "
        + "join Allergene a on ai.allergene = a "
        + "where a.id not in (:ids)")
    List<Recipe> getAllWithoutAllergens(@Param("ids") Set<Long> allergeneIds);

    @Query("select distinct ri.recipe from RecipeIngredient ri "
        + "left join Ingredient i on ri.ingredient = i "
        + "left join AllergeneIngredient ai on ai.ingredient = i "
        + "join Allergene a on ai.allergene = a "
        + "where a.id not in (:allergeneIds)"
        + "and i.id in (:ingredientIds)")
    List<Recipe> getAllWithIngredientsWithoutAllergens(@Param("ingredientIds") Set<Long> ingredientIds, @Param("allergeneIds") Set<Long> allergeneIds);

    @Transactional
    default void updateIngredients(Long id, Set<RecipeIngredient> ingredients) {
        Recipe recipe = findById(id).orElse(null);
        if (recipe != null) {
            recipe.setIngredients(ingredients);
            save(recipe);
        }
    }

    Recipe findFirstByOrderByIdDesc();

    Recipe findFirstByOrderByIdAsc();
}
