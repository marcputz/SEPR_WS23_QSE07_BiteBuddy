package at.ac.tuwien.sepr.groupphase.backend.service.validation;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe.RecipeDetailsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe.RecipeIngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Ingredient;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.IngredientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ByteArrayInputStream;

@Component
public class RecipeValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final IngredientRepository ingredientRepository;

    public RecipeValidator(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    public static boolean isValidImage(byte[] imageData) {
        LOGGER.trace("isValidImage()");

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
            BufferedImage image = ImageIO.read(inputStream);
            return image != null;
        } catch (IOException e) {
            return false;
        }
    }

    public void validateForCreate(RecipeDetailsDto recipe) throws ValidationException, ConflictException {
        LOGGER.trace("validateForCreate({})", recipe);

        List<String> validationErrors = new ArrayList<>();
        List<String> conflictErrors = new ArrayList<>();

        if (recipe != null) {
            if (recipe.picture() != null && !isValidImage(recipe.picture())) {
                validationErrors.add("Image has to be valid");
            } else if (recipe.picture() == null) {
                validationErrors.add("Image is required");
            }

            if (recipe.name().trim().isEmpty()) {
                validationErrors.add("Recipe Name cannot be empty");
            }

            if (recipe.name().length() > 255) {
                validationErrors.add("Recipe Name is too long");
            }

            if (recipe.description() == null || recipe.description().trim().isEmpty()) {
                validationErrors.add("Description of Recipe cannot be empty");
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
