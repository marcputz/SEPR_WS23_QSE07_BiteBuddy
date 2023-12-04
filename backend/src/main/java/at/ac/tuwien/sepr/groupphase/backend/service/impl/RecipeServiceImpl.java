package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Recipe;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeIngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.RecipeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecipeServiceImpl implements RecipeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private RecipeRepository recipeRepository;
    private RecipeIngredientRepository recipeIngredientRepository;

    @Autowired
    public RecipeServiceImpl(RecipeRepository recipeRepository, RecipeIngredientRepository recipeIngredientRepository) {
        this.recipeRepository = recipeRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
    }

    @Override
    public RecipeListDto searchRecipes(RecipeSearchDto searchParams) {
        LOGGER.debug("search recipes");

        List<Recipe> recipes = this.recipeRepository.findAll();

        // TODO filtering

        ArrayList<RecipeDto> recipeDtos = new ArrayList<>();
        for (Recipe recipe : recipes) {
            // adding recipes when maxCount -1, or we don't reach maxCount yet
            if ((searchParams.maxCount() > 0 && recipeDtos.size() < searchParams.maxCount()) || (searchParams.maxCount() == -1)) {
                recipeDtos.add(new RecipeDto(recipe.getId(), null, null, recipe.getName(), recipe.getInstructions()));
            }
        }

        return new RecipeListDto(recipeDtos);
    }
}
