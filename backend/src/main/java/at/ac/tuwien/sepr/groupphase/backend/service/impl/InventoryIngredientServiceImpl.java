package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InventoryIngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeDetailsDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.MenuPlan;
import at.ac.tuwien.sepr.groupphase.backend.entity.MenuPlanContent;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.InventoryIngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.MenuPlanRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.InventoryIngredientService;
import at.ac.tuwien.sepr.groupphase.backend.service.MenuPlanService;
import at.ac.tuwien.sepr.groupphase.backend.service.RecipeService;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.time.LocalDate;

public class InventoryIngredientServiceImpl implements InventoryIngredientService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final InventoryIngredientRepository inventoryIngredientRepository;
    private final MenuPlanRepository menuPlanRepository;
    private final MenuPlanService menuPlanService;
    private final RecipeService recipeService;

    public InventoryIngredientServiceImpl(InventoryIngredientRepository inventoryIngredientRepository, MenuPlanRepository menuPlanRepository,
                                          MenuPlanService menuPlanService, RecipeService recipeService) {
        this.inventoryIngredientRepository = inventoryIngredientRepository;
        this.menuPlanRepository = menuPlanRepository;
        this.menuPlanService = menuPlanService;
        this.recipeService = recipeService;
    }

    @Override
    public void createInventory(ApplicationUser user) {
        MenuPlan menuPlan = this.menuPlanService.getMenuPlanForUserOnDate(user, LocalDate.now());

        if (menuPlan != null) {
            for (MenuPlanContent content : menuPlan.getContent()) {
                Long recipeId = content.getRecipe().getId();

                RecipeDetailsDto recipeDetails = this.recipeService.getDetailedRecipe(recipeId);

                // for each ingredient add it to the inventory
            }
        }

        throw new NotImplementedException();
    }

    @Override
    public List<InventoryIngredientDto> searchInventory(ApplicationUser user, boolean onlyValid) {
        throw new NotImplementedException();
    }

    @Override
    public List<InventoryIngredientDto> searchInventory(Long menuPlanId) {
        throw new NotImplementedException();
    }

    @Override
    public void updateInventoryIngredient(ApplicationUser user, InventoryIngredientDto updatedIngredientDto) throws NotFoundException, ConflictException {
        throw new NotImplementedException();
    }

    @Override
    public void updateInventoryIngredient(InventoryIngredientDto updatedIngredientDto) throws NotFoundException, ConflictException {
        throw new NotImplementedException();
    }
}
