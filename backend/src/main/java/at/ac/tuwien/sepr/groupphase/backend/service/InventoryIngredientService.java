package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InventoryIngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;

import java.util.List;

public interface InventoryIngredientService {
    /**
     * Checks the MenuPlan for running recipes and adds all the ingredients to the inventory.
     * Only creates the inventory for running MenuPlans, not outdated ones!
     *
     * @param user for which we want to create the inventory.
     */
    void createInventory(ApplicationUser user);

    /**
     * Searches the inventory for all ingredients matching the user.
     *
     * @param user      for which we want to get the inventory.
     * @param onlyValid if true we only look at the inventory for the MenuPlan which is still running. If false we return the whole inventory over all MenuPlans.
     * @return list of the inventory.
     */
    List<InventoryIngredientDto> searchInventory(ApplicationUser user, boolean onlyValid);

    /**
     * Returns the inventory for a specific MenuPlan.
     *
     * @param menuPlanId id of the MenuPlan which we want to lookup.
     * @return list of all matching inventory ingredients used in the specific MenuPlan.
     */
    List<InventoryIngredientDto> searchInventory(Long menuPlanId);

    /**
     * Updates a single inventory ingredient.
     *
     * @param user                 user of which we want to update an ingredient from
     * @param updatedIngredientDto updated inventory ingredient.
     * @throws NotFoundException if ingredient did not exist before
     * @throws ConflictException if ingredient was updated incorrectly
     */
    void updateInventoryIngredient(ApplicationUser user, InventoryIngredientDto updatedIngredientDto) throws NotFoundException, ConflictException;

    /**
     * Updates a single inventory ingredient. This should never be used in the endpoint for security reasons,
     * since we not validate if the user is authorized to change the inventory.
     *
     * @param updatedIngredientDto updated inventory ingredient.
     * @throws NotFoundException if ingredient did not exist before
     * @throws ConflictException if ingredient was updated incorrectly
     */
    void updateInventoryIngredient(InventoryIngredientDto updatedIngredientDto) throws NotFoundException, ConflictException;
}
