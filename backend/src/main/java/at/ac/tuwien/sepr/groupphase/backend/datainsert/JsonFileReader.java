package at.ac.tuwien.sepr.groupphase.backend.datainsert;

import at.ac.tuwien.sepr.groupphase.backend.entity.Recipe;
import at.ac.tuwien.sepr.groupphase.backend.entity.RecipeIngredient;
import at.ac.tuwien.sepr.groupphase.backend.entity.RecipeIngredientDetails;
import at.ac.tuwien.sepr.groupphase.backend.entity.Ingredient;
import at.ac.tuwien.sepr.groupphase.backend.entity.Allergene;
import at.ac.tuwien.sepr.groupphase.backend.entity.AllergeneIngredient;
import at.ac.tuwien.sepr.groupphase.backend.entity.FoodUnit;


import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.IngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.AllergeneRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeIngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeIngredientDetailsRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.AllergeneIngredientRepository;
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
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Profile("addJsonData")
@Component
public class JsonFileReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String DEFAULT_KEY_FOLDER = (new File("")).getAbsolutePath() + "/src/main/resources/data";
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
    private final RecipeIngredientDetailsRepository recipeIngredientDetailsRepository;

    public JsonFileReader(RecipeRepository recipeRepository, IngredientRepository ingredientRepository,
                          AllergeneRepository allergeneRepository, AllergeneIngredientRepository allergeneIngredientRepository,
                          RecipeIngredientRepository recipeIngredientRepository, RecipeIngredientDetailsRepository recipeIngredientDetailsRepository) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.allergeneRepository = allergeneRepository;
        this.allergeneIngredientRepository = allergeneIngredientRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.recipeIngredientDetailsRepository = recipeIngredientDetailsRepository;
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
            //            this aswell as later code are also for the pictures
            int pictureCount = 1;
            if (recipeRepository.count() == 0) {
                for (Recipe recipe : recipes) {
                    Path path = Path.of(DEFAULT_PICTURE_FOLDER + "/" + pictureCount + ".png");
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
                    r.setId(recipeIngredientString.getId());
                    r.setRecipe(recipeRepository.getById(recipeIngredientString.getRecipe()));
                    r.setIngredient(ingredientRepository.getById(recipeIngredientString.getIngredient()));
                    String detailsString = recipeIngredientString.getAmount();
                    RecipeIngredientDetails r1 = setRecipeIngredientDetails(detailsString);
                    recipeIngredientDetailsRepository.save(r1);
                    r.setAmount(r1);
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


            // ------------------ the following would do the pictures --------------------------------------

            /*for (long i = 1; i < 4; i++) {
                byte[] picture = recipeRepository.getById(i).getPicture();
                Path path2 = Paths.get(DEFAULT_PICTURE_FOLDER + "/" + i + "saved.png");
                LOGGER.info("Path where picture is saved: " + path2);
                Files.write(path2, picture);
            } */

        } catch (IOException e) {
            LOGGER.error("Error reading JSON file", e);
        }
    }

    private RecipeIngredientDetails setRecipeIngredientDetails(String s) {
        String remainingString = s;
        Float amount = null;
        FoodUnit foodUnit = null;
        String describer;
        if (!startsWithNumber(s)) {
            //amount = null;
        } else if (startsWithOunce(s)) {
            amount = extractNumbersIfStartsWithOunce(s);
            remainingString = extractStringAfterNumericValueBrackets(s);
            remainingString = removeFirstWord(remainingString);
            foodUnit = FoodUnit.ounce;
        } else if (startsWithFraction(s)) {
            amount = parseFraction(s);
            remainingString = getRemainingStringAfterFraction(s);
        } else {
            amount = extractNumericValueBeforeUnit(s);
            remainingString = extractStringAfterNumericValueNormal(s);
        }
        remainingString = removeTextInParentheses(remainingString);
        describer = extractSubstringAfterComma(remainingString);
        remainingString = extractSubstringBeforeComma(remainingString);
        if (isFirstWordFoodUnit(remainingString)) {
            foodUnit = convertFirstWordToFoodUnit(remainingString);
            remainingString = removeFirstWord(remainingString);
        }
        String ingredient = remainingString;
        RecipeIngredientDetails r = new RecipeIngredientDetails();
        r.setUnit(foodUnit);
        r.setDescriber(describer);
        r.setIngredient(ingredient);
        if (amount != null) {
            r.setAmount(amount);
        }
        return r;
    }

    private static boolean startsWithNumber(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        char firstChar = input.charAt(0);
        return Character.isDigit(firstChar);
    }

    public static boolean startsWithFraction(String input) {
        // Define a regex pattern for matching "m/n" at the beginning of the string, where m and n are integers
        String regexPattern = "^\\d+/\\d+.*";

        // Use Pattern.matches() to check if the input string matches the pattern
        return Pattern.matches(regexPattern, input);
    }

    public static float parseFraction(String input) {
        // Define a regex pattern to match the format m/n where m and n are integers
        String regexPattern = "^(\\d+)/(\\d+).*";

        // Use Pattern and Matcher to extract numerator and denominator
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regexPattern);
        java.util.regex.Matcher matcher = pattern.matcher(input);

        // Check if the pattern matches the input
        if (matcher.matches()) {
            // Extract numerator and denominator as strings
            String numeratorStr = matcher.group(1);
            String denominatorStr = matcher.group(2);

            // Convert strings to integers
            int numerator = Integer.parseInt(numeratorStr);
            int denominator = Integer.parseInt(denominatorStr);

            // Check for division by zero
            if (denominator != 0) {
                // Return the result as a float
                return (float) numerator / denominator;
            } else {
                // Handle division by zero case
                throw new IllegalArgumentException("Denominator cannot be zero.");
            }
        } else {
            // Handle the case where the input doesn't match the expected format
            throw new IllegalArgumentException("Invalid input format. Expected m/n.");
        }
    }

    public static String getRemainingStringAfterFraction(String input) {
        // Define a regex pattern to match the format m/n where m and n are integers
        String regexPattern = "^(\\d+)/(\\d+)(.*)";

        // Use Pattern and Matcher to extract numerator, denominator, and remaining string
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(input);

        // Check if the pattern matches the input
        if (matcher.matches()) {
            // Extract remaining string
            String remainingString = matcher.group(3);

            // Return the remaining string
            return remainingString;
        } else {
            // Handle the case where the input doesn't match the expected format
            throw new IllegalArgumentException("Invalid input format. Expected m/n.");
        }
    }

    private boolean startsWithOunce(String amount) {
        String patternString = "^(\\d+) \\(\\d+ ounce\\)";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(amount);
        return matcher.find();
    }

    private static float extractNumbersIfStartsWithOunce(String amount) {
        String patternString = "^(\\d+) \\((\\d+) ounce\\)";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(amount);

        if (matcher.find()) {
            int firstNumber = Integer.parseInt(matcher.group(1));
            double secondNumber = Double.parseDouble(matcher.group(2));
            return (float) (firstNumber * secondNumber); // Cast to int for the final result
        } else {
            return 0;
        }
    }

    private Float extractNumericValueBeforeUnit(String amount) {
        Pattern pattern = Pattern.compile("(\\d+(?: \\d+/\\d+)?)\\s*(\\S+)");
        Matcher matcher = pattern.matcher(amount);

        if (matcher.find()) {
            String numericPart = matcher.group(1);
            String unitPart = matcher.group(2);

            Float numericValue = (float) parseNumericPart(numericPart);
            return numericValue;
        } else {
            // Return some default value or handle the case where the pattern doesn't match
            return 0.0F;
        }
    }

    private double parseNumericPart(String numericPart) {
        if (numericPart.contains("/")) {
            // Handle fractions (e.g., 1/4, 3/4)
            String[] parts = numericPart.split(" ");
            if (parts.length == 2) {
                // If there are two parts (e.g., 1 1/4), add them together
                int whole = Integer.parseInt(parts[0]);
                String[] fractionParts = parts[1].split("/");
                int numerator = Integer.parseInt(fractionParts[0]);
                int denominator = Integer.parseInt(fractionParts[1]);
                return whole + (double) numerator / denominator;
            } else {
                // If there is only one part (e.g., 1/4), directly parse it
                String[] fractionParts = numericPart.split("/");
                int numerator = Integer.parseInt(fractionParts[0]);
                int denominator = Integer.parseInt(fractionParts[1]);
                return (double) numerator / denominator;
            }
        } else {
            // Handle whole numbers
            return Double.parseDouble(numericPart);
        }
    }

    private static String extractStringAfterNumericValueNormal(String amount) {
        Pattern pattern = Pattern.compile("\\d+(?: \\d+/\\d+)?\\s*(.+)");
        Matcher matcher = pattern.matcher(amount);

        if (matcher.find()) {
            return matcher.group(1).trim();
        } else {
            // Return the original string or handle the case where the pattern doesn't match
            return amount;
        }
    }

    private static String extractStringAfterNumericValueBrackets(String amount) {
        Pattern pattern = Pattern.compile("\\d+(?: \\d+/\\d+)?\\s*\\((\\d+\\s*\\S+\\s*\\S*)\\)\\s*(.+)");
        Matcher matcher = pattern.matcher(amount);

        if (matcher.find()) {
            String unitPart = matcher.group(1).trim();
            String restOfString = matcher.group(2).trim();

            // Remove the word after the closing parenthesis from the extracted string
            if (restOfString.startsWith(unitPart)) {
                restOfString = restOfString.substring(unitPart.length()).trim();
            }

            return restOfString;
        } else {
            // Return the original string or handle the case where the pattern doesn't match
            return amount;
        }
    }

    private static String removeTextInParentheses(String input) {
        Pattern pattern = Pattern.compile("\\([^)]+\\)");
        Matcher matcher = pattern.matcher(input);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(result, "");
        }
        matcher.appendTail(result);

        return result.toString().trim();
    }

    private static String extractSubstringAfterComma(String amount) {
        int commaIndex = amount.indexOf(',');
        if (commaIndex != -1) {
            // Check if there's a space after the comma and adjust the substring accordingly
            int startIndex = amount.charAt(commaIndex + 1) == ' ' ? commaIndex + 2 : commaIndex + 1;
            return amount.substring(startIndex).trim();
        } else {
            // Return the original string or handle the case where there's no comma
            return "";
        }
    }

    private static String extractSubstringBeforeComma(String amount) {
        int commaIndex = amount.indexOf(',');
        if (commaIndex != -1) {
            return amount.substring(0, commaIndex).trim();
        } else {
            // Return the original string or handle the case where there's no comma
            return amount;
        }
    }

    private static boolean isFirstWordFoodUnit(String amount) {
        // Extract the first word from the string
        String[] words = amount.split("\\s+");
        if (words.length > 0) {
            String firstWord = words[0].toLowerCase(); // Convert to lowercase for case-insensitive comparison

            // Check if the first word is a superstring of a word in the FoodUnit enum
            for (FoodUnit foodUnit : FoodUnit.values()) {
                if (firstWord.toLowerCase().contains(foodUnit.name())) {
                    return true;
                }
            }
        }

        return false;
    }

    private static FoodUnit convertFirstWordToFoodUnit(String amount) {
        // Extract the first word from the string
        String firstWord = amount.split("\\s+")[0].toLowerCase(); // Convert to lowercase for case-insensitive comparison

        // Find the corresponding FoodUnit enum value
        for (FoodUnit foodUnit : FoodUnit.values()) {
            if (firstWord.toLowerCase().contains(foodUnit.name().toLowerCase())) {
                return foodUnit;
            }
        }
        return null;
    }

    private static String removeFirstWord(String input) {
        // Split the string into words
        String[] words = input.split("\\s+");

        // Remove the first word and join the remaining words
        if (words.length > 1) {
            return String.join(" ", Arrays.copyOfRange(words, 1, words.length));
        } else {
            // Handle the case where there is only one word or an empty string
            return "";
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
