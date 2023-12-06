package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeDetailsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Recipe;
import at.ac.tuwien.sepr.groupphase.backend.entity.RecipeIngredient;
import at.ac.tuwien.sepr.groupphase.backend.repository.AllergeneIngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeIngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.RecipeService;

import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.hibernate.query.spi.Limit;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class RecipeServiceImpl implements RecipeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private RecipeRepository recipeRepository;
    private RecipeIngredientRepository recipeIngredientRepository;

    private AllergeneIngredientRepository allergeneIngredientRepository;

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

    public RecipeDetailsDto getDetailedRecipe(long id) {
        // TODO check if ID is valid (not null)
        LOGGER.trace("details({})", id);
        Optional<Recipe> recipe = this.recipeRepository.findById(id);
        if(recipe.isEmpty()){
            throw new NotFoundException("The searched for recipe does not exist in the database anymore.");
        }
        else{
            RecipeDetailsDto detailsDto = new RecipeDetailsDto(id, recipe.get().getName(), recipe.get().getInstructions());
            return detailsDto;
        }
        /*if(recipe.isEmpty()){
            throw new NotFoundException("The searched for recipe does not exist in the database anymore.");
        }
        else{
            List<RecipeIngredient> ingredients = this.recipeIngredientRepository.findByRecipe(recipe.get());

            if(ingredients.isEmpty()){
                throw new NotFoundException("The searched for recipe does not have any ingredients");
            }
            else{
                ArrayList<AllergeneIngredientsDTO> recipeDtos = new ArrayList<>();
                for(RecipeIngredient recipeIngredient : ingredients){
                    this.allergeneIngredientRepository.findByIngredient(recipeIngredient.getIngredient());
                }

            }
        }


        return null;*/
    }
}
