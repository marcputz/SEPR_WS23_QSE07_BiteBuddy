package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.RecipeIngredientDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeIngredientDetailsRepository extends JpaRepository<RecipeIngredientDetails, Long> {
    /**
     * Finds all recipe ingredient details where the specific ingredient matches the given name.
     *
     * @param name for which we want to filter.
     * @return List of {@link RecipeIngredientDetails} which include given name.
     * @author Frederik Skiera
     */
    List<RecipeIngredientDetails> findByIngredientContainingIgnoreCase(String name);
}
