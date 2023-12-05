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

    public void validateForUpdate(ApplicationUser applicationUser) throws ValidationException {
        LOGGER.trace("validateForUpdate({})", applicationUser);
        List<String> validationErrors = new ArrayList<>();

        if (applicationUser.getId() == null) {
            validationErrors.add("No ID given");
        }

        basicValidation(applicationUser, validationErrors);

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of ApplicationUser for update failed", validationErrors);
        }
    }

    private void basicValidation(ApplicationUser applicationUser, List<String> validationErrors) {
        //TODO Complete Validation
        if (applicationUser.getEmail() == null || applicationUser.getEmail().trim().isEmpty()) {
            validationErrors.add("Email is required");
        } else if (!isValidEmail(applicationUser.getEmail())) {
            validationErrors.add("Invalid email format");
        }

        if (applicationUser.getPasswordEncoded() == null || applicationUser.getPasswordEncoded().trim().isEmpty()) {
            validationErrors.add("Password is required");
        } else if (applicationUser.getPasswordEncoded().length() < 8) { // Assuming minimum 8 characters for password
            validationErrors.add("Password must be at least 8 characters long");
        }

        if (applicationUser.getNickname() == null || applicationUser.getNickname().trim().isEmpty()) {
            validationErrors.add("Nickname is required");
        } else if (applicationUser.getNickname().length() > 255) {
            validationErrors.add("Nickname cannot be longer than 255 characters");
        }
    }

    public boolean isValidEmail(String email) {
        // Simple regex for email validation.
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
        return pattern.matcher(email).matches();
    }
}
