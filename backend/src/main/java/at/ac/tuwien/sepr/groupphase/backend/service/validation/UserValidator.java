package at.ac.tuwien.sepr.groupphase.backend.service.validation;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class UserValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    public void validateForCreate(ApplicationUser applicationUser) throws ValidationException {
        LOGGER.trace("validateForCreate({})", applicationUser);
        List<String> validationErrors = new ArrayList<>();
        basicValidation(applicationUser, validationErrors);
        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Your account could not be created", validationErrors);
        }
    }

    public void validateForUpdate(ApplicationUser applicationUser) throws ValidationException {
        LOGGER.trace("validateForUpdate({})", applicationUser);
        List<String> validationErrors = new ArrayList<>();
        if (applicationUser.getId() == null) {
            validationErrors.add("No ID given");
        }
        basicValidation(applicationUser, validationErrors);
        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Your account details couldn't be updated", validationErrors);
        }
    }

    private void basicValidation(ApplicationUser applicationUser, List<String> validationErrors) {
        if (applicationUser.getEmail() == null || applicationUser.getEmail().trim().isEmpty()) {
            validationErrors.add("Email is required");
        } else if (!isPossibleEmail(applicationUser.getEmail())) {
            validationErrors.add("Invalid email format");
        } else if (applicationUser.getEmail().length() > 255) {
            validationErrors.add("Email cannot be longer than 255 characters");
        }

        if (applicationUser.getPasswordEncoded() == null || applicationUser.getPasswordEncoded().trim().isEmpty()) {
            validationErrors.add("Password is required");
        } else if (applicationUser.getPasswordEncoded().length() < 8) {
            validationErrors.add("Password has to be at least 8 characters long");
        }

        if (applicationUser.getNickname() == null || applicationUser.getNickname().trim().isEmpty()) {
            validationErrors.add("Nickname is required");
        } else if (applicationUser.getNickname().length() > 255) {
            validationErrors.add("Nickname cannot be longer than 255 characters");
        }
    }

    public boolean isPossibleEmail(String email) {
        // Simple regex for email validation.
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
        return pattern.matcher(email).matches();
    }
}
