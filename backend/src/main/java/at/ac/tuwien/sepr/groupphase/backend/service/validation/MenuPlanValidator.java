package at.ac.tuwien.sepr.groupphase.backend.service.validation;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InventoryIngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InventoryListDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Ingredient;
import at.ac.tuwien.sepr.groupphase.backend.entity.MenuPlan;
import at.ac.tuwien.sepr.groupphase.backend.entity.MenuPlanContent;
import at.ac.tuwien.sepr.groupphase.backend.entity.Profile;
import at.ac.tuwien.sepr.groupphase.backend.entity.RecipeIngredient;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.IngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeIngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.IngredientService;
import at.ac.tuwien.sepr.groupphase.backend.service.RecipeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class MenuPlanValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final RecipeService recipeService;

    public MenuPlanValidator(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    public void validateForUpdate(MenuPlan menuplan) throws ValidationException {
        LOGGER.trace("validateForUpdate({})", menuplan);
        List<String> validationErrors = new ArrayList<>();

        if (menuplan.getUser() == null) {
            validationErrors.add("User must be set to a value");
        }
        if (menuplan.getProfile() == null) {
            validationErrors.add("Profile must be set to a value");
        }
        if (menuplan.getFromDate() == null) {
            validationErrors.add("From Date must be set to a value");
        }
        if (menuplan.getUntilDate() == null) {
            validationErrors.add("Until Date must be set to a value");
        }

        // check if timeframes are set correctly
        if (menuplan.getFromDate() != null && menuplan.getUntilDate() != null) {
            if (menuplan.getUntilDate().isBefore(menuplan.getFromDate())) {
                validationErrors.add("Until Date cannot be before From Date");
            }
            int numDays = (int) Duration.between(menuplan.getFromDate().atStartOfDay(), menuplan.getUntilDate().atStartOfDay()).toDays() + 1;
            if (numDays != 7) {
                validationErrors.add("Menu Plan timeframe must be over exactly 7 days");
            }
        }

        // check contents
        Set<MenuPlanContent> contents = menuplan.getContent();
        if (contents == null) {
            validationErrors.add("Content cannot be NULL value");
        } else {
            // check if content variables are valid
            for (MenuPlanContent c : contents) {
                if (c.getDayIdx() < 0) {
                    validationErrors.add("Content cannot have negative Day Index");
                }
                if (c.getTimeslot() < 0) {
                    validationErrors.add("Content cannot have negative Timeslot");
                }
                if (c.getMenuplan() == null) {
                    validationErrors.add("Content (Day: " + c.getDayIdx() + " / Timeslot: " + c.getTimeslot() + " has NULL as MenuPlan value");
                } else {
                    long menuplanId = c.getMenuplan().getId();
                    if (menuplanId != menuplan.getId()) {
                        validationErrors.add("Content (Day: " + c.getDayIdx() + " / Timeslot: " + c.getTimeslot() + " has wrong MenuPlan value set");
                    }
                }
            }

            // check if there are multiple contents for one timeslot
            List<String> usedDayAndTimeslotCombinations = new ArrayList<>();
            for (MenuPlanContent c : contents) {
                String combination = c.getDayIdx() + "-" + c.getTimeslot();
                if (usedDayAndTimeslotCombinations.contains(combination)) {
                    // combination already exists
                    validationErrors.add("There are multiple contents defined on day " + c.getDayIdx() + " on timeslot " + c.getTimeslot());
                } else {
                    usedDayAndTimeslotCombinations.add(combination);
                }
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of menu plan for update failed", validationErrors);
        }

    }

    public void validateForCreate(ApplicationUser user, Profile profile, LocalDate fromDate, LocalDate untilDate) throws ValidationException {
        LOGGER.trace("validateForCreate({},{},{},{})", user, profile, fromDate, untilDate);
        List<String> validationErrors = new ArrayList<>();

        // check if all values are not null
        if (user == null) {
            validationErrors.add("User cannot be NULL value");
        } else {
            if (profile == null) {
                validationErrors.add("Profile cannot be NULL value");
            } else {
                if (fromDate == null || untilDate == null) {
                    validationErrors.add("Dates cannot be NULL values");
                } else {

                    // check if timeframes are set correctly
                    if (untilDate.isBefore(fromDate)) {
                        validationErrors.add("Until Date cannot be before From Date");
                    }
                    int numDays = (int) Duration.between(fromDate.atStartOfDay(), untilDate.atStartOfDay()).toDays() + 1;
                    if (numDays != 7) {
                        validationErrors.add("Menu Plan timeframe must be over exactly 7 days");
                    }

                }
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of menu plan for create failed", validationErrors);
        }

    }


    public void validateFridge(List<String> fridge) throws ValidationException, ConflictException {
        LOGGER.trace("validateFridge({})", fridge);
        List<String> validationErrors = new ArrayList<>();

        if (fridge != null && !fridge.isEmpty()) {
            // check that every String exists in either Ingredients or RID.ingredients
            for (String ingredient : fridge) {
                List<String> allIngredients = this.recipeService.findMatchingIngredients(ingredient);

                if (allIngredients.isEmpty()) {
                    validationErrors.add("Ingredient " + ingredient + " does not exist");
                }
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of menu plan for create failed", validationErrors);
        }
    }

    public void validateInventoryIngredientForUpdate(InventoryIngredientDto ingredientDto, InventoryListDto currentInventory) throws ValidationException {
        LOGGER.trace("validateInventoryIngredientForUpdate({}, {})", ingredientDto, currentInventory);
        List<String> validationErrors = new ArrayList<>();

        if (ingredientDto != null) {
            InventoryIngredientDto inverseIngredientDto = new InventoryIngredientDto(
                ingredientDto.getId(), ingredientDto.getName(), ingredientDto.getMenuPlanId(), ingredientDto.getIngredientId(),
                ingredientDto.getDetailedName(), ingredientDto.getAmount(), ingredientDto.getUnit(), !ingredientDto.isInventoryStatus()
            );
            boolean foundIngredient = false;

            if (!currentInventory.missing().isEmpty()) {
                // find if we find matching one but status is irrelevant
                for (InventoryIngredientDto existing : currentInventory.missing()) {
                    if (existing.equals(inverseIngredientDto)) {
                        foundIngredient = true;
                        break;
                    }
                }
            }

            if (!currentInventory.available().isEmpty() && !foundIngredient) {
                // find if we find matching one but status is irrelevant
                for (InventoryIngredientDto existing : currentInventory.available()) {
                    if (existing.equals(inverseIngredientDto)) {
                        foundIngredient = true;
                        break;
                    }
                }
            }

            if (!foundIngredient) {
                validationErrors.add("Cannot add new ingredients to the inventory");
            }

            if (currentInventory.missing().isEmpty() && currentInventory.available().isEmpty()) {
                validationErrors.add("Cannot update an empty inventory");
            }


        } else {
            validationErrors.add("Ingredient to update has to exist");
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of menu plan for create failed", validationErrors);
        }
    }
}
