package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.AllergeneDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.lang.invoke.MethodHandles;

@RestController
@RequestMapping(path = ProfileEndpoint.BASE_PATH)
public class ProfileEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    static final String BASE_PATH = "/api/v1/profiles";

    private final ProfileService profileService;

    public ProfileEndpoint(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Operation(summary = "Create profile", description = "Profile fields should be valid")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "422", description = "Unable to process the data, because contains invalid data"),
        @ApiResponse(responseCode = "400", description = "Illegal arguments in the request body") })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileDto post(@Valid @RequestBody ProfileDto toCreateProfile) throws ValidationException {
        LOGGER.info("Received POST request on {}", BASE_PATH);
        LOGGER.debug("Request body for POST:\n{}", toCreateProfile);

        return profileService.saveProfile(toCreateProfile);
    }
}
