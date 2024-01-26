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
    List<RecipeIngredient> findByRecipe(Recipe recipe);

    List<RecipeIngredient> findByIngredient_Id(Long id);

    List<RecipeIngredient> findByAmountEquals(RecipeIngredientDetails details);
}
