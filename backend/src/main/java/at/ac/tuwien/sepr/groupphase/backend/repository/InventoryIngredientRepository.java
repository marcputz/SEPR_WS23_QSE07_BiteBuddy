package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.InventoryIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InventoryIngredientRepository extends JpaRepository<InventoryIngredient, Long> {
    List<InventoryIngredient> findAllyByMenuPlanId(long id);

    @Query("select i.name from InventoryIngredient i where i.menuPlanId = :menuplanId and i.inventoryStatus = TRUE ")
    List<String> getOwnedIngredientsByMenuPlanId(@Param("menuplanId") long menuplanId);
}
