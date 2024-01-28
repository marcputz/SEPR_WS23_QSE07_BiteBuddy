package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.InventoryIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InventoryIngredientRepository extends JpaRepository<InventoryIngredient, Long> {
    /**
     * Finds all ingredients which are used in a specific menu plan.
     *
     * @param id of the specific Menu Plan.
     * @return List of {@link InventoryIngredient}.
     * @author Frederik Skiera
     */
    List<InventoryIngredient> findAllByMenuPlanId(long id);

    /**
     * Gets all ingredients which are available for a specific menu plan.
     *
     * @param menuplanId of the specific menu plan.
     * @return List of ingredients as string.
     * @author Marc Putz
     */
    @Query("select i.name from InventoryIngredient i where i.menuPlanId = :menuplanId and i.inventoryStatus = TRUE ")
    List<String> getOwnedIngredientsByMenuPlanId(@Param("menuplanId") long menuplanId);
}
