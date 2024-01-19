package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InventoryIngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InventoryListDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.InventoryIngredient;
import at.ac.tuwien.sepr.groupphase.backend.entity.MenuPlan;
import at.ac.tuwien.sepr.groupphase.backend.entity.MenuPlanContent;
import at.ac.tuwien.sepr.groupphase.backend.entity.Recipe;
import at.ac.tuwien.sepr.groupphase.backend.entity.RecipeIngredient;
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
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.time.LocalDate;

@Service
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
        // MenuPlan menuPlan = this.menuPlanService.getMenuPlanForUserOnDate(user, LocalDate.now());
        MenuPlan menuPlan = null;

        // TODO this is a workaround since getMenuPlanForUserOnDate is not implemented
        List<MenuPlan> menuPlans = this.menuPlanService.getAllMenuPlansOfUserDuringTimeframe(user, LocalDate.now().minusDays(5), LocalDate.now());

        if (menuPlans.size() == 1) {
            menuPlan = menuPlans.get(0);
        }

        if (menuPlan != null) {
            HashMap<Long, InventoryIngredient> newInventory = new HashMap<>();
            HashMap<String, InventoryIngredient> newNewInventory = new HashMap<>();

            for (MenuPlanContent content : menuPlan.getContent()) {
                Recipe recipe = content.getRecipe();

                // for each ingredient add it to the inventory
                for (RecipeIngredient recipeIngredient : recipe.getIngredients()) {
                    // if it does not exist we just add it
                    if (!newNewInventory.containsKey(recipeIngredient.getIngredient().getName())) {
                        newNewInventory.put(recipeIngredient.getIngredient().getName(), new InventoryIngredient(
                            recipeIngredient.getIngredient().getName(), menuPlan.getId(), recipeIngredient.getId(),
                            recipeIngredient.getAmount().getAmount(),
                            recipeIngredient.getAmount().getUnit(), false
                        ));
                    } else if (newNewInventory.get(recipeIngredient.getIngredient().getName()).getUnit() == recipeIngredient.getAmount().getUnit()) {
                        InventoryIngredient existingEntry = newNewInventory.get(recipeIngredient.getIngredient().getName());

                        Float newAmount = existingEntry.getAmount();
                        // combining them if none of them are null
                        if (recipeIngredient.getAmount().getAmount() != null && newAmount != null) {
                            newAmount += recipeIngredient.getAmount().getAmount();
                        }

                        InventoryIngredient newEntry = new InventoryIngredient(existingEntry.getName(), menuPlan.getId(), existingEntry.getIngredientId(), newAmount,
                            existingEntry.getUnit(), false);

                        newNewInventory.put(recipeIngredient.getIngredient().getName(), newEntry);
                    } else {
                        // different unit, not adding them up together
                        newNewInventory.put(recipeIngredient.getIngredient().getName(), new InventoryIngredient(
                            recipeIngredient.getIngredient().getName(), menuPlan.getId(), recipeIngredient.getId(),
                            recipeIngredient.getAmount().getAmount() != null ? recipeIngredient.getAmount().getAmount() : 0.0f,
                            recipeIngredient.getAmount().getUnit(), false));
                    }
                }
            }
            this.inventoryIngredientRepository.saveAll(newNewInventory.values());
        }
    }

    @Override
    public InventoryListDto searchInventory(ApplicationUser user, boolean onlyValid) {
        if (onlyValid) {
            // MenuPlan menuPlan = this.menuPlanService.getMenuPlanForUserOnDate(user, LocalDate.now());

            // TODO this is a workaround since getMenuPlanForUserOnDate is not implemented
            MenuPlan menuPlan = null;
            List<MenuPlan> menuPlans = this.menuPlanService.getAllMenuPlansOfUserDuringTimeframe(user, LocalDate.now().minusDays(5), LocalDate.now());

            if (menuPlans.size() == 1) {
                menuPlan = menuPlans.get(0);

                return this.searchInventory(menuPlan.getId());
            } else {
                return null;
            }
        } else {
            // not sure if needed
            throw new NotImplementedException();
        }
    }

    @Override
    public InventoryListDto searchInventory(Long menuPlanId) {
        List<InventoryIngredientDto> missing = new ArrayList<>();
        List<InventoryIngredientDto> available = new ArrayList<>();
        if (menuPlanId != null) {
            List<InventoryIngredient> inventory = this.inventoryIngredientRepository.findAllyByMenuPlanId(menuPlanId);

            for (InventoryIngredient ingred : inventory) {
                InventoryIngredientDto newDto = new InventoryIngredientDto(ingred.getId(), ingred.getName(), ingred.getMenuPlanId(), ingred.getIngredientId(), ingred.getAmount(),
                    ingred.getUnit(), ingred.getInventoryStatus());

                if (ingred.getInventoryStatus()) {
                    available.add(newDto);
                } else {
                    missing.add(newDto);
                }
            }
        }
        return new InventoryListDto(missing, available);
    }

    @Override
    public void updateInventoryIngredient(ApplicationUser user, InventoryIngredientDto updatedIngredientDto) throws NotFoundException, ConflictException {
        // TODO Validation
        InventoryIngredient toSave = this.inventoryIngredientRepository.findById(updatedIngredientDto.getId()).get();
        toSave.setInventoryStatus(updatedIngredientDto.isInventoryStatus());
        this.inventoryIngredientRepository.save(toSave);
    }

    @Override
    public void updateInventoryIngredient(InventoryIngredientDto updatedIngredientDto) throws NotFoundException, ConflictException {
        throw new NotImplementedException();
    }
}
