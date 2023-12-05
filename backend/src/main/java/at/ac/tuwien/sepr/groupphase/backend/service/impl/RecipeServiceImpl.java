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
import org.hibernate.query.spi.Limit;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
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
    public List<RecipeListDto> searchRecipes(RecipeSearchDto searchParams) {
        LOGGER.debug("search recipes");

        String name = "";
        String creator = "";

        // checking searchParams
        if (searchParams != null) {
            name = !searchParams.name().trim().isEmpty() ? searchParams.name() : "";
            creator = !searchParams.creator().trim().isEmpty() ? searchParams.creator() : "";
        }

        List<Recipe> recipes = this.recipeRepository.findByNameContainingIgnoreCase(name);

        ArrayList<RecipeListDto> recipeDtos = new ArrayList<>();
        for (Recipe recipe : recipes) {
            // adding recipes when maxCount -1, or we don't reach maxCount yet
            recipeDtos.add(new RecipeListDto(null, recipe.getName(), recipe.getId()));
        }

        return recipeDtos;
    }
}
