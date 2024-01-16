package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.InventoryIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryIngredientRepository extends JpaRepository<InventoryIngredient, Long> {
    List<InventoryIngredient> findAllyByMenuPlanId(long id);
}
