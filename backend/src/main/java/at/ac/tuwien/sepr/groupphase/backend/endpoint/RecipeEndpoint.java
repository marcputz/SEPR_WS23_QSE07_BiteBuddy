package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeDetailsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.RecipeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping(value = RecipeEndpoint.BASE_PATH)
public class RecipeEndpoint {
    public static final String BASE_PATH = "/api/v1/recipes";
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final RecipeService recipeService;

    @Autowired
    public RecipeEndpoint(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @PostMapping()
    public RecipeSearchResultDto searchRecipes(@RequestBody RecipeSearchDto searchParams) {
        LOGGER.info("POST " + BASE_PATH);
        LOGGER.debug("request body: {}", searchParams);
        return this.recipeService.searchRecipes(searchParams);
    }

    @GetMapping("/ingredient/{name}")
    public List<String> findMatchingIngredients(@PathVariable String name) {
        LOGGER.info("GET " + BASE_PATH + "/ingredient/" + name);
        return this.recipeService.findMatchingIngredients(name);
    }

    @GetMapping("/ingredient/basic/{name}")
    public List<String> findOnlyBasicMatchingIngredients(@PathVariable String name) {
        LOGGER.info("GET " + BASE_PATH + "/ingredient/" + name);
        return this.recipeService.findMatchingIngredients(name);
    }

    @PostMapping("/create")
    public void createRecipe(@RequestBody RecipeDetailsDto recipe) {
        LOGGER.info("POST " + BASE_PATH + "/create");
        LOGGER.debug("request body: {}", recipe);
        try {
            this.recipeService.createRecipe(recipe);
        } catch (ConflictException e) {
            HttpStatus status = HttpStatus.CONFLICT;
            logClientError(status, "Conflict while creating recipe", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        } catch (ValidationException e) {
            HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
            logClientError(status, "Recipe parameters where not valid", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }
    }

    @GetMapping("/{id}")
    public RecipeDetailsDto getDetailedRecipe(@PathVariable long id) {
        LOGGER.info("GET " + BASE_PATH + "/" + id);
        try {
            return this.recipeService.getDetailedRecipe(id);
        } catch (NotFoundException e) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            logClientError(status, "Recipe to lookup not found:", e);
            throw new ResponseStatusException(status, e.getMessage(), e);
        }
    }

    private void logClientError(HttpStatus status, String message, Exception e) {
        LOGGER.warn("{} {}: {}: {}", status.value(), message, e.getClass().getSimpleName(), e.getMessage());
    }
}
