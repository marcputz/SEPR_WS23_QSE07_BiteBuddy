package at.ac.tuwien.sepr.groupphase.backend.service.validation;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.type.AllergensEU;
import at.ac.tuwien.sepr.groupphase.backend.type.FoodPreference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProfileValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Validate the given profile for creation.
     *
     * @param profile the profile to validate
     * @throws ValidationException if the profile or any fields are invalid
     */
    public void validateForCreate(ProfileDto profile) throws ValidationException {
        LOGGER.trace("validateForCreate({})", profile);
        List<String> validationErrors = new ArrayList<>();

        validateUsername(profile, validationErrors);
        validateAllergens(profile, validationErrors);
        validateFoodPreference(profile, validationErrors);

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of horse for update failed", validationErrors);
        }
    }

    //Validator method used in update and create methods
    /**
     * Validate the given profile's username.
     *
     * @param profile the profile to validate
     * @param validationErrors the list of validation errors to add to
     */
    private void validateUsername(ProfileDto profile, List<String> validationErrors) {
        LOGGER.trace("validateUsername({})", profile);

        if (hasStringWhitespace(profile.getUserName())) {
            validationErrors.add("Username cannot consist of only white spaces");
        }
        //is username unique= conflict exception
    }

    /**
     * Validate the given profile's allergens.
     *
     * @param profile the profile to validate
     * @param validationErrors the list of validation errors to add to
     */
    private void validateAllergens(ProfileDto profile, List<String> validationErrors) {
        LOGGER.trace("validateAllergens({})", profile);

        if (hasWhitespaceAllergensObject(profile.getAllergens())
            || hasAllAllergensWhitespaceObjects(profile.getAllergens())) {
            validationErrors.add("Allergens cannot consist of only white spaces");
        } else if (profile.getAllergens().contains(AllergensEU.NOT_KNOWN) && profile.getAllergens().size() > 1) {
            validationErrors.add("Allergens cannot contain 'not known' and other allergens");
        }
    }

    /**
     * Validate the given profile's food preferences.
     *
     * @param profile the profile to validate
     * @param validationErrors the list of validation errors to add to
     */
    private void validateFoodPreference(ProfileDto profile, List<String> validationErrors) {
        LOGGER.trace("validateFoodPreference({})", profile);

        if (hasAllFoodPreferencesWhitespaceObjects(profile.getFoodPreference())
            || hasWhitespaceFoodPreferencesObject(profile.getFoodPreference())) {
            validationErrors.add("Food preference cannot consist of only white spaces");
        } else if (profile.getFoodPreference().contains(FoodPreference.VEGAN) && profile.getFoodPreference().contains(FoodPreference.VEGETARIAN)) {
            validationErrors.add("Food preference cannot contain both vegan and vegetarian");
        } else if (profile.getFoodPreference().contains(FoodPreference.VEGAN) && profile.getFoodPreference().contains(FoodPreference.MEAT)) {
            validationErrors.add("Food preference cannot contain both vegan and meat");
        } else if (profile.getFoodPreference().contains(FoodPreference.VEGETARIAN) && profile.getFoodPreference().contains(FoodPreference.MEAT)) {
            validationErrors.add("Food preference cannot contain both vegetarian and meat");
        } else if ((profile.getFoodPreference().contains(FoodPreference.VEGAN) && profile.getFoodPreference().contains(FoodPreference.FISH))
                || (profile.getFoodPreference().contains(FoodPreference.VEGETARIAN) && profile.getFoodPreference().contains(FoodPreference.FISH))) {
            validationErrors.add("Please select the pesceterian option");
        } else if ((profile.getFoodPreference().contains(FoodPreference.VEGAN) && profile.getFoodPreference().contains(FoodPreference.EGG))
            || (profile.getFoodPreference().contains(FoodPreference.VEGETARIAN) && profile.getFoodPreference().contains(FoodPreference.EGG))) {
            validationErrors.add("Please select the ovo-vegetarian option");
        } else if (profile.getFoodPreference().contains(FoodPreference.NONE) && profile.getFoodPreference().size() > 1) {
            validationErrors.add("Food preferences cannot contain 'none' and other food preferences");
        }
    }

    //helper method for username validation
    private boolean hasStringWhitespace(String element) {
        LOGGER.trace("hasStringWhitespace({})", element);
        return element != null && element.trim().isEmpty();
    }

    private boolean hasAllAllergensWhitespaceObjects(List<AllergensEU> list) {
        return list.stream()
            .allMatch(obj -> obj.toString().trim().isEmpty());
    }
    private boolean hasAllFoodPreferencesWhitespaceObjects(List<FoodPreference> list) {
        return list.stream()
            .allMatch(obj -> obj.toString().trim().isEmpty());
    }
    private boolean hasWhitespaceAllergensObject(List<AllergensEU > list) {
        return list.stream()
            .anyMatch(obj -> obj.toString().contains(" "));
    }
    private boolean hasWhitespaceFoodPreferencesObject(List<FoodPreference> list) {
        return list.stream()
            .anyMatch(obj -> obj.toString().contains(" "));
    }
}
