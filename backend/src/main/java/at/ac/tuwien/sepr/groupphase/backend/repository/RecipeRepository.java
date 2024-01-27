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

    @Query("select distinct r from Recipe r "
        + "left join RecipeIngredient ri on ri.recipe = r "
        + "left join AllergeneIngredient ai on ai.ingredient = ri.ingredient "
        + "where ai.allergene.id not in (:ids)"
        + "or ai.allergene is null")
    List<Recipe> getAllWithoutAllergens(@Param("ids") Set<Long> allergeneIds);

    @Query("select distinct ri.recipe from RecipeIngredient ri "
        + "left join RecipeIngredientDetails rid on ri.amount = rid "
        + "left join AllergeneIngredient ai on ai.ingredient = ri.ingredient "
        + "where (ai.allergene.id not in (:allergeneIds) or ai.allergene is null) "
        + "and (ri.ingredient.name in (:ingredientNames) or rid.ingredient in (:ingredientNames))")
    List<Recipe> getAllWithIngredientsWithoutAllergens(@Param("ingredientNames") Set<String> ingredientNames, @Param("allergeneIds") Set<Long> allergeneIds);

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
