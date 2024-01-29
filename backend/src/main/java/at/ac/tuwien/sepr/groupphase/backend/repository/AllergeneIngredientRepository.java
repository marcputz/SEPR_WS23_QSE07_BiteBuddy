package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.AllergeneIngredient;
import at.ac.tuwien.sepr.groupphase.backend.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllergeneIngredientRepository extends JpaRepository<AllergeneIngredient, Long> {

    /**
     * Finds allergenIngredients which belong to a certain ingredient.
     *
     * @param ingredient ingredient id we want the allergeneIngredients from.
     * @return List of {@link AllergeneIngredient} which all are part of a specific ingredient.
     * @author Thomas Hellweger
     */
    List<AllergeneIngredient> findByIngredient(Ingredient ingredient);
}
