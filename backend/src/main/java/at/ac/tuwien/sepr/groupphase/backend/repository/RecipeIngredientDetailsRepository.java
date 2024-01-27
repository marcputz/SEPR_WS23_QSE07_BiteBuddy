package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.RecipeIngredientDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeIngredientDetailsRepository extends JpaRepository<RecipeIngredientDetails, Long> {
    List<RecipeIngredientDetails> findByIngredientContainingIgnoreCase(String name);
}
