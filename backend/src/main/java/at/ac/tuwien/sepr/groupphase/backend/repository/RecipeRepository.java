package at.ac.tuwien.sepr.groupphase.backend.repository;


import at.ac.tuwien.sepr.groupphase.backend.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;


@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByNameContainingIgnoreCase(String name);
    Set<Recipe> findAllByRecipeIngredientsIngredientId(long l);

}
