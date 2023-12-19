package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.AllergeneDto;
import at.ac.tuwien.sepr.groupphase.backend.service.AllergeneService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping(path = AllergeneEndpoint.BASE_PATH)
public class AllergeneEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    static final String BASE_PATH = "/api/v1/allergens";

    private final AllergeneService allergeneService;

    public AllergeneEndpoint(AllergeneService allergeneService) {
        this.allergeneService = allergeneService;
    }

    @Operation(summary = "Get allergens", description = "Get all allergens from database")
    @GetMapping
    public List<AllergeneDto> getAllAllergens() {
        LOGGER.info("Received GET request on {}", BASE_PATH);
        return allergeneService.getAllAllergens();
    }
}
