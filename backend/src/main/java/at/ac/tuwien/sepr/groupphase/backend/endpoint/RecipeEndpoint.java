package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.service.RecipeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
    public List<RecipeListDto> searchRecipes(@RequestBody RecipeSearchDto searchParams) {
        LOGGER.info("GET " + BASE_PATH);
        LOGGER.debug("request body: {}", searchParams);
        return this.recipeService.searchRecipes(searchParams);
    }

    // @GetMapping("/{id}")
    // public RecipeDto getDetailedRecipe(@PathVariable long id) {
    //     LOGGER.info("GET " + BASE_PATH + id);
    //     return this.recipeService.getDetailedRecipe(id);
    // }
}
