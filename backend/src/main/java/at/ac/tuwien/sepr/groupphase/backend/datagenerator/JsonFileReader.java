package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.Recipe;
import at.ac.tuwien.sepr.groupphase.backend.entity.Ingredient;
import at.ac.tuwien.sepr.groupphase.backend.entity.Allergene;
import at.ac.tuwien.sepr.groupphase.backend.entity.AllergeneIngredient;
import at.ac.tuwien.sepr.groupphase.backend.entity.RecipeIngredient;


import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeIngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.AllergeneRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.AllergeneIngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.IngredientRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.lang.invoke.MethodHandles;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Profile("generateData")
@Component
public class JsonFileReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String DEFAULT_KEY_FOLDER = (new File("")).getAbsolutePath() + "/src/main/resources/FoodDataFiles";

    private static final String PRIVATE_KEY_FILENAME_RECIPES = "Recipes.json";
    private static final String PRIVATE_KEY_FILENAME_INGREDIENTS = "Ingredients.json";
    private static final String PRIVATE_KEY_FILENAME_ALLERGENES = "Allergenes.json";
    private static final String PRIVATE_KEY_FILENAME_ALLERGENEINGREDIENTS = "AllergeneIngredients.json";
    private static final String PRIVATE_KEY_FILENAME_RECIPEINGREDIENTS = "RecipeIngredients.json";
    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final AllergeneRepository allergeneRepository;
    private final AllergeneIngredientRepository allergeneIngredientRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;

    public JsonFileReader(RecipeRepository recipeRepository, IngredientRepository ingredientRepository,
                          AllergeneRepository allergeneRepository, AllergeneIngredientRepository allergeneIngredientRepository,
                          RecipeIngredientRepository recipeIngredientRepository) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.allergeneRepository = allergeneRepository;
        this.allergeneIngredientRepository = allergeneIngredientRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
    }

    @PostConstruct
    public void putFoodDataInDataBase() {
        LOGGER.info("called putting FoodData into Database from File");

        try {
            File fileIngredients = new File(DEFAULT_KEY_FOLDER, PRIVATE_KEY_FILENAME_INGREDIENTS);
            File fileAllergenes = new File(DEFAULT_KEY_FOLDER, PRIVATE_KEY_FILENAME_ALLERGENES);

            ObjectMapper objectMapper = new ObjectMapper();

            LOGGER.info("putting FoodData into Database from File");


            Ingredient[] ingredients = objectMapper.readValue(fileIngredients, Ingredient[].class);
            if (ingredientRepository.count() == 0) {
                LOGGER.info("putting ingredients");
                ingredientRepository.saveAll(Arrays.asList(ingredients));
            }
            Allergene[] allergenes = objectMapper.readValue(fileAllergenes, Allergene[].class);
            if (allergeneRepository.count() == 0) {
                LOGGER.info("putting allergenes");
                allergeneRepository.saveAll(Arrays.asList(allergenes));
            }
            File fileRecepies = new File(DEFAULT_KEY_FOLDER, PRIVATE_KEY_FILENAME_RECIPES);
            Recipe[] recipes = objectMapper.readValue(fileRecepies, Recipe[].class);
            if (recipeRepository.count() == 0) {
                LOGGER.info("putting recipes");
                recipeRepository.saveAll(Arrays.asList(recipes));
            }
            File fileAllergeneIngredients = new File(DEFAULT_KEY_FOLDER, PRIVATE_KEY_FILENAME_ALLERGENEINGREDIENTS);
            File fileRecipeIngredients = new File(DEFAULT_KEY_FOLDER, PRIVATE_KEY_FILENAME_RECIPEINGREDIENTS);

            AllergeneIngredientString[] allergeneIngredients = objectMapper.readValue(fileAllergeneIngredients, AllergeneIngredientString[].class);
            if (allergeneRepository.count() == 0) {
                for (AllergeneIngredientString allergeneIngredient : allergeneIngredients) {
                    LOGGER.info("putting allergeneIngredients");
                    AllergeneIngredient a = new AllergeneIngredient().setId(allergeneIngredient.getId());
                    //a.setAllergene(allergeneRepository.getById(allergeneIngredient.getAllergene()));
                    //a.setIngredient(ingredientRepository.getById(allergeneIngredient.getIngredient()));
                    //LOGGER.info("putting allergeneIngredients ingredient: " + a.getIngredient());
                    allergeneIngredientRepository.save(a);
                }
            }
            RecipeIngredientString[] recipeIngredients = objectMapper.readValue(fileRecipeIngredients, RecipeIngredientString[].class);
            if (recipeIngredientRepository.count() == 0) {
                for (RecipeIngredientString recipeIngredientString : recipeIngredients) {
                    LOGGER.info("putting recipeIngredients");
                    RecipeIngredient r = new RecipeIngredient().setId(recipeIngredientString.id);
                    //r.setRecipe(recipeRepository.getById(recipeIngredientString.getRecipe()));
                    //r.setIngredient(ingredientRepository.getById(recipeIngredientString.getIngredient()));
                    r.setAmount(recipeIngredientString.amount);
                    recipeIngredientRepository.save(r);

                }
            }
            /*
             */
        } catch (IOException e) {
            LOGGER.error("Error reading JSON file", e);
        }

    }

    public static class RecipeIngredientString {

        private Long id;
        private Long recipe;
        private Long ingredient;
        private String amount;

        public RecipeIngredientString() {
        }

        public Long getId() {
            return id;
        }

        public Long getIngredient() {
            return ingredient;
        }

        public Long getRecipe() {
            return recipe;
        }

        public String getAmount() {
            return amount;
        }

    }

    public static class AllergeneIngredientString {

        private Long id;
        private Long allergene;
        private Long ingredient;

        public AllergeneIngredientString() {
        }

        public Long getId() {
            return id;
        }

        public Long getAllergene() {
            return allergene;
        }

        public Long getIngredient() {
            return ingredient;
        }

    }
}
