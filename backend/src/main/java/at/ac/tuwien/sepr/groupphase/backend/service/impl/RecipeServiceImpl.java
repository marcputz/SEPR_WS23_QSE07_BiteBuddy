package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeDetailsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.repository.*;
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
    private IngredientRepository ingredientRepository;
    private AllergeneIngredientRepository allergeneIngredientRepository;
    private AllergeneRepository allergeneRepository;

    @Autowired
    public RecipeServiceImpl(RecipeRepository recipeRepository, RecipeIngredientRepository recipeIngredientRepository, IngredientRepository ingredientRepository, AllergeneIngredientRepository allergeneIngredientRepository, AllergeneRepository allergeneRepository) {
        this.recipeRepository = recipeRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.ingredientRepository = ingredientRepository;
        this.allergeneIngredientRepository = allergeneIngredientRepository;
        this.allergeneRepository = allergeneRepository;
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
            recipeDtos.add(new RecipeListDto(null, recipe.getName(), recipe.getId(), recipe.getPicture()));
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


        /*TODO: make dtos for allergenes and ingredients, which contain the information needed to display them in details, when the database is working.
           Ingredients require name and amount, allergenes only the name (and id additionally for both if acces would be required in the future).
           Transform data into those dtos and add a list of both to the recipedetails dto*/
        if (recipe.isEmpty()) {
            throw new NotFoundException("The searched for recipe does not exist in the database anymore.");
        }
        else {
            List<RecipeIngredient> ingredients = this.recipeIngredientRepository.findByRecipe(recipe.get());
            ArrayList<String> ingredientsAndAmount = new ArrayList<>();
            for (RecipeIngredient ingredient : ingredients){
                Ingredient currentIngredient = ingredient.getIngredient();
                ingredientsAndAmount.add(currentIngredient.getName() + ": " + ingredient.getAmount());
            }


           if(ingredients.isEmpty()){
                throw new NotFoundException("The searched for recipe does not have any ingredients");
            }
            else{
                ArrayList<String> allergens = new ArrayList<>();
                for(RecipeIngredient recipeIngredient : ingredients){
                    System.out.println(recipeIngredient.getIngredient());
                    List<AllergeneIngredient> allergensIngredient = this.allergeneIngredientRepository.findByIngredient(recipeIngredient.getIngredient());
                    System.out.println(allergensIngredient);
                    for (AllergeneIngredient allergene : allergensIngredient){
                        System.out.println(allergene.getAllergene().getName());
                        allergens.add(allergene.getAllergene().getName());
                        /*Optional<Allergene> currentAllergene = allergeneRepository.findById(allergene.getId());
                        if(currentAllergene.isPresent()){
                            System.out.println("Added Allergene");
                            allergens.add(currentAllergene.get().getName());
                        }*/

                    }
                }
               System.out.println(ingredients);
               System.out.println(allergens);;
               RecipeDetailsDto detailsDto = new RecipeDetailsDto(id, recipe.get().getName(), recipe.get().getInstructions(), ingredientsAndAmount, allergens, recipe.get().getPicture());
               return detailsDto;


            }


        }

        }
        //return null;
    }
}
