package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    Set<Ingredient> findAllByRecipeIngredientsRecipeId(long l);

    Set<Ingredient> findAllByAllergeneIngredientsAllergeneId(long l);

    List<Ingredient> findByNameContainingIgnoreCase(String name);
}
