package at.ac.tuwien.sepr.groupphase.backend.service.validation;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Profile;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
     * @throws ValidationException if the rating is invalid
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

    /**
     * Validate the profile to delete.
     *
     * @param currentUser the current User of the application
     * @param profile the currently used profile
     * @throws ConflictException if the profile there is a conflict with already existing data (profile is active profile, profile does not belong to active user)
     * @author Thomas Hellweger
     */
    public void validateDelete(ApplicationUser currentUser, Profile profile) throws ConflictException {
        LOGGER.trace("validateDelete({}),({})", currentUser, profile);
        List<String> conflictErrors = new ArrayList<>();

        if (Objects.equals(currentUser.getActiveProfile().getId(), profile.getId())) {
            conflictErrors.add("The active profile can not be deleted.");
        }

        if (!Objects.equals(profile.getUser().getId(), currentUser.getId())) {
            conflictErrors.add("The profile does not belong to the active user");
        }

        if (!conflictErrors.isEmpty()) {
            throw new ConflictException("Deletion of Profile has conflict errors", conflictErrors);
        }
    }

    //helper method for username validation
    private boolean hasStringWhitespace(String element) {
        LOGGER.trace("hasStringWhitespace({})", element);
        return element != null && element.trim().isEmpty();
    }
}
