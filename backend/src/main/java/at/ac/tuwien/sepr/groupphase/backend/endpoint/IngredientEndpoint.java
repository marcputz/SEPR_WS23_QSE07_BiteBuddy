package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.IngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.service.IngredientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<IngredientDto> getAllIngredients() {
        LOGGER.info("Received GET request on {}", BASE_PATH);
        return ingredientService.getAllIngredients();
    }

    @GetMapping("/{name}")
    @ResponseStatus(HttpStatus.OK)
    public List<String> getAllMatchingIngredients(@PathVariable String name) {
        LOGGER.info(BASE_PATH + "/" + name);
        return ingredientService.getNamesMatching(name);
    }
}
