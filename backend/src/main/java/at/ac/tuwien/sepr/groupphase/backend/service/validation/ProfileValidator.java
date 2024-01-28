package at.ac.tuwien.sepr.groupphase.backend.service.validation;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;

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

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of horse for update failed", validationErrors);
        }
    }

    /**
     * Validate the given profile's username.
     *
     * @param profile the profile to validate
     * @param validationErrors the list of validation errors to add to
     */
    private void validateUsername(ProfileDto profile, List<String> validationErrors) {
        LOGGER.trace("validateUsername({})", profile);

        if (hasStringWhitespace(profile.getName())) {
            validationErrors.add("Name cannot consist of only white spaces");
        }
    }


    /**
     * Validate the ratings rating integer.
     *
     * @param rating the rating to validate
     */
    public void validateRating(int rating) throws ValidationException {
        LOGGER.trace("validateRating({})", rating);
        List<String> validationErrors = new ArrayList<>();

        if (rating != 0 && rating != 1) {
            validationErrors.add("rating has to be either 0 or 1");
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of rating for rating for recipe rating failed", validationErrors);
        }
    }

    //helper method for username validation
    private boolean hasStringWhitespace(String element) {
        LOGGER.trace("hasStringWhitespace({})", element);
        return element != null && element.trim().isEmpty();
    }
}
