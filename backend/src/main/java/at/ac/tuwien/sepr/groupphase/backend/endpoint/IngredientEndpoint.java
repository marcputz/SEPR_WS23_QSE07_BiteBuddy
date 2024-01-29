package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.IngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.service.IngredientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

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

    /**
     * Gets all ingredients.
     *
     * @return {@link IngredientDto} of all ingredients.
     * @author Giulia Gallico
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<IngredientDto> getAllIngredients() {
        LOGGER.info("Received GET request on {}", BASE_PATH);
        return ingredientService.getAllIngredients();
    }

    /**
     * Returns a list of all ingredients matching the given name. The search is not case-sensitive.
     *
     * @param name for which we to search ingredients.
     * @return List of ingredients names matching searched name.
     * @author Frederik Skiera
     */
    @GetMapping("/{name}")
    @ResponseStatus(HttpStatus.OK)
    public List<String> getAllMatchingIngredients(@PathVariable String name) {
        LOGGER.info(BASE_PATH + "/" + name);
        return ingredientService.getNamesMatching(name);
    }
}
