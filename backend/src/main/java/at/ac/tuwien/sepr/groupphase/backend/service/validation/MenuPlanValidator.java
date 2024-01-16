package at.ac.tuwien.sepr.groupphase.backend.service.validation;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Profile;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class MenuPlanValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public void validateForCreate(ApplicationUser user, Profile profile, LocalDate fromDate, LocalDate untilDate) throws ValidationException {
        LOGGER.trace("validateForCreate({},{},{},{})", user, profile, fromDate, untilDate);
        List<String> validationErrors = new ArrayList<>();

        // check if all values are not null
        if (user == null) {
            validationErrors.add("User cannot be NULL value");
        } else {
            // TODO: add check for profile once it's implemented
            // if (profile == null) {
            //     validationErrors.add("Profile cannot be null");
            /* } else*/ {
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
}
