package at.ac.tuwien.sepr.groupphase.backend.operationalDataInsert;


import at.ac.tuwien.sepr.groupphase.backend.entity.Ingredient;
import at.ac.tuwien.sepr.groupphase.backend.entity.Allergene;
import at.ac.tuwien.sepr.groupphase.backend.entity.RecipeIngredient;
import at.ac.tuwien.sepr.groupphase.backend.entity.Recipe;
import at.ac.tuwien.sepr.groupphase.backend.entity.AllergeneIngredient;


import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeIngredientRepository;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@Profile("addJsonData")
@Component
public class JsonFileReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String DEFAULT_KEY_FOLDER = (new File("")).getAbsolutePath() + "/src/main/resources/FoodDataFiles";
    private static final String DEFAULT_PICTURE_FOLDER = (new File("")).getAbsolutePath() + "/src/main/resources/RecipePictures";
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
            File fileRecepies = new File(DEFAULT_KEY_FOLDER, PRIVATE_KEY_FILENAME_RECIPES);
            File fileAllergeneIngredients = new File(DEFAULT_KEY_FOLDER, PRIVATE_KEY_FILENAME_ALLERGENEINGREDIENTS);
            File fileRecipeIngredients = new File(DEFAULT_KEY_FOLDER, PRIVATE_KEY_FILENAME_RECIPEINGREDIENTS);

            ObjectMapper objectMapper = new ObjectMapper();

            Allergene[] allergenes = objectMapper.readValue(fileAllergenes, Allergene[].class);
            Ingredient[] ingredients = objectMapper.readValue(fileIngredients, Ingredient[].class);
            Recipe[] recipes = objectMapper.readValue(fileRecepies, Recipe[].class);
            AllergeneIngredientString[] allergeneIngredients = objectMapper.readValue(fileAllergeneIngredients, AllergeneIngredientString[].class);
            RecipeIngredientString[] recipeIngredients = objectMapper.readValue(fileRecipeIngredients, RecipeIngredientString[].class);

            if (ingredientRepository.count() == 0) {
                ingredientRepository.saveAll(Arrays.asList(ingredients));
            }
            if (allergeneRepository.count() == 0) {
                allergeneRepository.saveAll(Arrays.asList(allergenes));
            }

            int pictureCount = 1;
            if (recipeRepository.count() == 0) {
                for (Recipe recipe : recipes) {
                    Path path = Path.of(DEFAULT_PICTURE_FOLDER + "/" + pictureCount + ".png");
                    LOGGER.info("Path where picture is gotten: " + path);
                    recipe.setPicture(Files.readAllBytes(path));
                    recipeRepository.save(recipe);
                    pictureCount++;
                }
            }
            if (allergeneIngredientRepository.count() == 0) {
                for (AllergeneIngredientString allergeneIngredient : allergeneIngredients) {
                    AllergeneIngredient a = new AllergeneIngredient();
                    a.setId(allergeneIngredient.getId());
                    a.setAllergene(allergeneRepository.getById(allergeneIngredient.getAllergene()));
                    a.setIngredient(ingredientRepository.getById(allergeneIngredient.getIngredient()));
                    allergeneIngredientRepository.save(a);
                }
            }
            if (recipeIngredientRepository.count() == 0) {
                for (RecipeIngredientString recipeIngredientString : recipeIngredients) {
                    RecipeIngredient r = new RecipeIngredient();
                    r.setId(recipeIngredientString.id);
                    r.setRecipe(recipeRepository.getById(recipeIngredientString.getRecipe()));
                    r.setIngredient(ingredientRepository.getById(recipeIngredientString.getIngredient()));
                    r.setAmount(recipeIngredientString.amount);
                    recipeIngredientRepository.save(r);
                }
            }


            /*
            //The following shows the functionality of the DB, works if uncommented

            // get all informations from recipeId to ingredientId to AllergeneId
            LOGGER.info("get all information only from RecipeID");
            Recipe recipe1 = recipeRepository.getById(3L);
            LOGGER.info(recipe1.toString());
            Set<Ingredient> ingredientsForRecipe = ingredientRepository.findAllByRecipeIngredientsRecipeId(recipe1.getId());
            for (Ingredient ingredient : ingredientsForRecipe) {
                LOGGER.info(ingredient.toString());
                Set<Allergene> allergenesForIngredient2 = allergeneRepository.findAllByAllergeneIngredientsIngredientId(ingredient.getId());
                for (Allergene allergene : allergenesForIngredient2) {
                    LOGGER.info(allergene.toString());
                }
            }

            // get all information from AllergeneId to ingredientId to recipeId
            LOGGER.info("get all information only from AllergeneID");
            Allergene allergene1 = allergeneRepository.getById(2L);
            LOGGER.info(allergene1.toString());
            Set<Ingredient> ingredientsForAllergene = ingredientRepository.findAllByAllergeneIngredientsAllergeneId(allergene1.getId());
            for (Ingredient ingredient : ingredientsForAllergene) {
                LOGGER.info(ingredient.toString());
                Set<Recipe> recipesForIngredient2 = recipeRepository.findAllByRecipeIngredientsIngredientId(ingredient.getId());
                for (Recipe recipe : recipesForIngredient2) {
                    LOGGER.info(recipe.toString());
                }
            }

            */
            // get all the pictures from the recipes and save them in RecipePictures
            for (long i = 1; i < 4; i++) {
                byte[] picture = recipeRepository.getById(i).getPicture();
                Path path2 = Paths.get(DEFAULT_PICTURE_FOLDER + "/PicFromDB/ " + i + ".png");
                LOGGER.info("Path where picture is saved: " + path2);
                Files.write(path2, picture);
            }

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
