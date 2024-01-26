package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.IngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.service.IngredientService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping(path = IngredientEndpoint.BASE_PATH)
public class IngredientEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    static final String BASE_PATH = "/api/v1/ingredients";

    private final IngredientService ingredientService;

    public IngredientEndpoint(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @Operation(summary = "Get ingredients", description = "Get all ingredient from database")
    @GetMapping
    public List<IngredientDto> getAllIngredients() {
        LOGGER.info("Received GET request on {}", BASE_PATH);
        return ingredientService.getAllIngredients();
    }

    @GetMapping("/{name}")
    public List<String> getAllMatchingIngredients(@PathVariable String name) {
        LOGGER.info(BASE_PATH + "/" + name);
        return ingredientService.getNamesMatching(name);
    }
}
