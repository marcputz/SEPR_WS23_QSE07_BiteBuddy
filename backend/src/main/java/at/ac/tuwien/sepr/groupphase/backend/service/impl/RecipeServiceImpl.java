package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeDetailsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.repository.*;
import at.ac.tuwien.sepr.groupphase.backend.service.RecipeService;

import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class RecipeServiceImpl implements RecipeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private RecipeRepository recipeRepository;
    private RecipeIngredientRepository recipeIngredientRepository;
    private IngredientRepository ingredientRepository;
    private AllergeneIngredientRepository allergeneIngredientRepository;
    private AllergeneRepository allergeneRepository;

    @Autowired
    public RecipeServiceImpl(RecipeRepository recipeRepository, RecipeIngredientRepository recipeIngredientRepository,
                             IngredientRepository ingredientRepository, AllergeneIngredientRepository allergeneIngredientRepository,
                             AllergeneRepository allergeneRepository) {
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
            // creator = !searchParams.creator().trim().isEmpty() ? searchParams.creator() : "";
        }

        List<Recipe> recipes = this.recipeRepository.findByNameContainingIgnoreCase(name);

        ArrayList<RecipeListDto> recipeDtos = new ArrayList<>();
        for (Recipe recipe : recipes) {
            recipeDtos.add(new RecipeListDto(null, recipe.getName(), recipe.getId(), recipe.getPicture()));
        }

        return recipeDtos;
    }

    @Override
    public void createRecipe(RecipeDetailsDto recipe) throws ConflictException {
        LOGGER.debug("createRecipe");

        // check ingredients exist
        Set<RecipeIngredient> ingredients = new HashSet<>();
        ArrayList<String> conflictList = new ArrayList<>();

        // creating database entry
        Recipe newRecipe = new Recipe();
        newRecipe.setPicture(recipe.picture());
        newRecipe.setName(recipe.name());
        newRecipe.setInstructions(recipe.description());
        newRecipe.setIngredients(ingredients);
        this.recipeRepository.save(newRecipe);

        // getting recipe id & checking if we can find the RecipeIngredients
        Recipe queriedRecipe = this.recipeRepository.findByNameContainingIgnoreCase(recipe.name()).get(0);

        // Trying to update recipe, but this does not work
        ingredients = new HashSet<>();
        for (String ingredient : recipe.ingredients()) {
            List<Ingredient> queriedResults = this.ingredientRepository.findByNameContainingIgnoreCase(ingredient);

            if (!queriedResults.isEmpty()) {
                RecipeIngredient ing = new RecipeIngredient();
                ing.setAmount("wenig");
                ing.setIngredient(queriedResults.get(0));
                ing.setRecipe(queriedRecipe);
                ingredients.add(ing);
                this.recipeIngredientRepository.save(ing);
            } else {
                conflictList.add("Ingredient " + ingredient + "does not exist");
            }
        }

        // checking that each recipe actually exists
        // (later when amount and everything is implemented the validation needs to be more complex and broad)
        if (!conflictList.isEmpty()) {
            this.recipeRepository.delete(queriedRecipe);
            throw new ConflictException("Ingredients do not match with the database", conflictList);
        }

        this.recipeRepository.updateIngredients(queriedRecipe.getId(), ingredients);
    }

    @Override
    public List<String> findMatchingIngredients(String name) {
        // TODO when we have ingredients with working amount and units we should return a dto of ingredients

        ArrayList<String> matchingIngredients = new ArrayList<>();
        List<Ingredient> ingredients = this.ingredientRepository.findByNameContainingIgnoreCase(name);

        for (int i = 0; i < 10; i++) {
            if (ingredients.size() > i) {
                matchingIngredients.add(ingredients.get(i).getName());
            }
        }
        return matchingIngredients;
    }

    @Override
    public void createRating(long recipeId, long userId, int rating) {
        LOGGER.trace("createRating({})({})", recipeId, userId);

    }


    @SuppressWarnings("checkstyle:CommentsIndentation")
    public RecipeDetailsDto getDetailedRecipe(long id) {
        // TODO check if ID is valid (not null)
        LOGGER.trace("details({})", id);
        Optional<Recipe> recipe = this.recipeRepository.findById(id);
        if (recipe.isEmpty()) {
            throw new NotFoundException("The searched for recipe does not exist in the database anymore.");
        } else {


            //TODO: make dtos for allergenes and ingredients, which contain the information needed to display them in details, when the database is working.
            // Ingredients require name and amount, allergenes only the name (and id additionally for both if acces would be required in the future).
            // Transform data into those dtos and add a list of both to the recipedetails dto
            List<RecipeIngredient> ingredients = this.recipeIngredientRepository.findByRecipe(recipe.get());
            ArrayList<String> ingredientsAndAmount = new ArrayList<>();
            for (RecipeIngredient ingredient : ingredients) {
                Ingredient currentIngredient = ingredient.getIngredient();
                ingredientsAndAmount.add(currentIngredient.getName() + ": " + ingredient.getAmount());
            }


            if (ingredients.isEmpty()) {
                throw new NotFoundException("The searched for recipe does not have any ingredients");
            } else {
                ArrayList<String> allergens = new ArrayList<>();
                for (RecipeIngredient recipeIngredient : ingredients) {
                    System.out.println(recipeIngredient.getIngredient());
                    List<AllergeneIngredient> allergensIngredient = this.allergeneIngredientRepository.findByIngredient(recipeIngredient.getIngredient());
                    System.out.println(allergensIngredient);
                    for (AllergeneIngredient allergene : allergensIngredient) {
                        System.out.println(allergene.getAllergene().getName());
                        allergens.add(allergene.getAllergene().getName());
                    }
                }
                RecipeDetailsDto detailsDto =
                        new RecipeDetailsDto(id, recipe.get().getName(), recipe.get().getInstructions(), ingredientsAndAmount, allergens,
                            recipe.get().getPicture());
                return detailsDto;
            }
        }

    }
}

