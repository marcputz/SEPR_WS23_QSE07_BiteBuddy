package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.InventoryIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InventoryIngredientRepository extends JpaRepository<InventoryIngredient, Long> {
    List<InventoryIngredient> findAllyByMenuPlanId(long id);

    // TODO: check which inventory status is defined for 'fridge' inventory and replace in SQL query
    @Query("select i.ingredientId from InventoryIngredient i where i.menuPlanId = :menuplanId and i.inventoryStatus = TRUE ")
    List<Long> getOwnedIngredientsByMenuPlanId(@Param("menuplanId") long menuplanId);
}
