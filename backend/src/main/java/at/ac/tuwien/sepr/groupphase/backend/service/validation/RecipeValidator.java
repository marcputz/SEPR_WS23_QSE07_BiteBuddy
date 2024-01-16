package at.ac.tuwien.sepr.groupphase.backend.service.validation;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeDetailsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeIngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Ingredient;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.IngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeIngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

@Component
public class RecipeValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private RecipeIngredientRepository recipeIngredientRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    public void validateForCreate(RecipeDetailsDto recipe) throws ValidationException, ConflictException {
        List<String> validationErrors = new ArrayList<>();
        List<String> conflictErrors = new ArrayList<>();

        if (recipe != null) {
            if (recipe.name().trim().isEmpty()) {
                validationErrors.add("Recipe Name cannot be empty");
            }

            if (recipe.name().length() > 255) {
                validationErrors.add("Recipe Name is too long");
            }

            // check that every recipe ingredient actually exists in the database
            if (!recipe.ingredients().isEmpty()) {
                for (RecipeIngredientDto ingredient : recipe.ingredients()) {
                    List<Ingredient> queriedResults = this.ingredientRepository.findByNameContainingIgnoreCase(ingredient.name());

                    if (queriedResults.isEmpty()) {
                        conflictErrors.add("Ingredient " + ingredient.name() + " does not exist");
                    }
                }
            } else {
                validationErrors.add("Recipe needs at least one ingredient");
            }

        } else {
            validationErrors.add("Recipe cannot be null");
        }


        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of Recipe for creation failed", validationErrors);
        }

        if (!conflictErrors.isEmpty()) {
            throw new ConflictException("Creation of Recipe has conflict errors", conflictErrors);
        }
    }
}
