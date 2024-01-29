package at.ac.tuwien.sepr.groupphase.backend.unittests.service;

import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.authentication.LoginDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe.RecipeDetailsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe.RecipeIngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe.RecipeListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe.RecipeSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe.RecipeSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Allergene;
import at.ac.tuwien.sepr.groupphase.backend.entity.AllergeneIngredient;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.FoodUnit;
import at.ac.tuwien.sepr.groupphase.backend.entity.Ingredient;
import at.ac.tuwien.sepr.groupphase.backend.entity.Picture;
import at.ac.tuwien.sepr.groupphase.backend.entity.Recipe;
import at.ac.tuwien.sepr.groupphase.backend.entity.RecipeIngredient;
import at.ac.tuwien.sepr.groupphase.backend.entity.RecipeIngredientDetails;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.AllergeneIngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.AllergeneRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.IngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PictureRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeIngredientDetailsRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeIngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthenticationService;
import at.ac.tuwien.sepr.groupphase.backend.service.RecipeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class RecipeServiceTest {
    private static final String DEFAULT_PICTURE_FOLDER = (new File("")).getAbsolutePath() + "/src/main/resources/RecipePictures";

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private RecipeIngredientRepository recipeIngredientRepository;

    @Autowired
    private RecipeIngredientDetailsRepository recipeIngredientDetailsRepository;

    @Autowired
    private AllergeneRepository allergeneRepository;

    @Autowired
    private AllergeneIngredientRepository allergeneIngredientRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private PictureRepository pictureRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationService authenticationService;

    private long recipe1Id;
    private long recipeIngredient1Id;
    private long allergene1Id;
    private long ingredient1Id;
    private long allergeneIngredient1Id;

    private long recipe2Id;
    private long recipeIngredient2Id;
    private long ingredient2Id;
    private long ingredient3Id;
    private long rd1Id;
    private long rd2Id;
    private ApplicationUser user;

    public String authenticate() throws Exception {
        LoginDto dto = new LoginDto();
        dto.setEmail("test@test");
        dto.setPassword("password");
        return this.authenticationService.loginUser(dto);
    }

    public static byte[] readJpegFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readAllBytes(path);
    }

    @BeforeEach
    public void setup() throws Exception {
        ApplicationUser user = new ApplicationUser();
        user.setNickname("testuser");
        user.setEmail("test@test");
        user.setPasswordEncoded(PasswordEncoder.encode("password", "test@test"));

        this.userRepository.save(user);

        this.user = this.userRepository.findByNickname("testuser").orElseThrow(NotFoundException::new);

        // adding picture
        Picture pic = new Picture();
        pic.setDescription("keine");
        pic.setData(readJpegFile(DEFAULT_PICTURE_FOLDER + "/1.jpg"));
        this.pictureRepository.save(pic);

        // creating ingredients
        // adding apple
        Ingredient ingredient1 = new Ingredient();
        ingredient1.setName("Apple");
        ingredient1Id = ingredientRepository.save(ingredient1).getId();

        // adding rice
        Ingredient ingredient2 = new Ingredient();
        ingredient2.setName("Rice");
        ingredient2Id = ingredientRepository.save(ingredient2).getId();

        // adding sugar
        Ingredient ingredient3 = new Ingredient();
        ingredient3.setName("sugar");
        ingredient3Id = ingredientRepository.save(ingredient3).getId();

        // creating recipes without ingredients
        Recipe recipe1 = new Recipe();
        recipe1.setCreatorId(-1L);
        recipe1.setInstructions("Instructions 1");
        recipe1.setName("recipe 1");
        recipe1.setPictureId(1L);
        recipe1Id = recipeRepository.save(recipe1).getId();

        Recipe recipe2 = new Recipe();
        recipe2.setCreatorId(this.user.getId());
        recipe2.setInstructions("Instructions2");
        recipe2.setName("recipe 2");
        recipe2.setPictureId(1L);
        recipe2Id = recipeRepository.save(recipe2).getId();


        // creating recipeIngredientDetails
        RecipeIngredientDetails r1 = new RecipeIngredientDetails();
        r1.setDescriber("little");
        r1.setUnit(FoodUnit.tablespoon);
        r1.setIngredient("sugar");
        r1.setAmount(1);
        rd1Id = recipeIngredientDetailsRepository.save(r1).getId();

        RecipeIngredientDetails r2 = new RecipeIngredientDetails();
        r2.setDescriber("much");
        r2.setUnit(FoodUnit.cup);
        r2.setIngredient("sugar");
        r2.setAmount(10);
        rd2Id = recipeIngredientDetailsRepository.save(r2).getId();


        // creating recipeIngredients
        RecipeIngredient recipeIngredient1 = new RecipeIngredient();
        recipeIngredient1.setIngredient(ingredient1);
        recipeIngredient1.setAmount(r1);
        recipeIngredient1.setRecipe(recipe1);
        recipeIngredient1Id = recipeIngredientRepository.save(recipeIngredient1).getId();

        RecipeIngredient recipeIngredient2 = new RecipeIngredient();
        recipeIngredient2.setIngredient(ingredient2);
        recipeIngredient2.setAmount(r2);
        recipeIngredient2.setRecipe(recipe2);
        recipeIngredient2Id = recipeIngredientRepository.save(recipeIngredient2).getId();

        // updating recipes with ingredients
        Set<RecipeIngredient> rIngredients1 = new HashSet<>();
        rIngredients1.add(recipeIngredient1);
        rIngredients1.add(recipeIngredient2);
        recipeRepository.updateIngredients(recipe1Id, rIngredients1);

        Set<RecipeIngredient> rIngredients2 = new HashSet<>();
        rIngredients2.add(recipeIngredient2);
        recipe2.setIngredients(rIngredients2);
        recipeRepository.updateIngredients(recipe2Id, rIngredients2);

        Allergene allergene1 = new Allergene();
        allergene1.setName("Fructose");
        Set<AllergeneIngredient> aIngredients = new HashSet<>();
        AllergeneIngredient allergeneIngredient1 = new AllergeneIngredient();
        allergeneIngredient1.setAllergene(allergene1);

        aIngredients.add(allergeneIngredient1);
        allergene1.setIngredients(aIngredients);
        allergeneIngredient1.setIngredient(ingredient1);

        allergene1Id = allergeneRepository.save(allergene1).getId();
        allergeneIngredient1Id = allergeneIngredientRepository.save(allergeneIngredient1).getId();
    }

    @AfterEach
    public void afterEach() {
        recipeIngredientRepository.deleteAll();
        recipeIngredientDetailsRepository.deleteAll();
        allergeneIngredientRepository.deleteAll();
        ingredientRepository.deleteAll();
        recipeRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void getAllAddedRecipes() {
        // creating request
        List<Recipe> allRecipes = this.recipeService.getAll();

        // asserting test
        assertNotNull(allRecipes);

        assertThat(allRecipes)
            .extracting(Recipe::getId, Recipe::getName, Recipe::getCreatorId)
            .contains(
                tuple(recipe1Id, "recipe 1", -1L),
                tuple(recipe2Id, "recipe 2", this.user.getId())
            );
    }

    @Test
    public void getFilteredRecipe() {
        // creating request
        RecipeSearchResultDto searchedRecipes = this.recipeService.searchRecipes(new RecipeSearchDto(
            "", "1", 0, 21
        ));

        // asserting test
        assertNotNull(searchedRecipes);

        assertThat(searchedRecipes.recipes())
            .extracting(RecipeListDto::id, RecipeListDto::name, RecipeListDto::creator)
            .contains(
                tuple(recipe1Id, "recipe 1", "BiteBuddy")
            );
    }

    @Test
    public void getCustomCreator() {
        // creating request
        RecipeSearchResultDto searchedRecipes = this.recipeService.searchRecipes(new RecipeSearchDto(
            "", "2", 0, 21
        ));

        // asserting test
        assertNotNull(searchedRecipes);

        assertThat(searchedRecipes.recipes())
            .extracting(RecipeListDto::id, RecipeListDto::name, RecipeListDto::creator)
            .contains(
                tuple(recipe2Id, "recipe 2", "testuser")
            );
    }

    @Test
    public void getRecipeDetailsOfExistingRecipe() throws Exception {
        RecipeDetailsDto details = this.recipeService.getDetailedRecipe(recipe1Id);

        // asserting test
        assertNotNull(details);

        assertAll(
            () -> assertThat(details.name())
                .contains(
                    "recipe 1"
                ),
            () -> assertThat(details.description())
                .contains(
                    "Instructions 1"
                ),
            () -> assertThat(details.ingredients())
                .hasSize(1)
                .contains(
                    new RecipeIngredientDto("sugar", 1.0f, FoodUnit.tablespoon)
                ),
            () -> assertThat(details.allergens())
                .hasSize(1)
                .contains("Fructose")
        );
    }

    @Test
    public void getRecipeDetailsOfNotExistingRecipe() {

        assertThrows(NotFoundException.class,
            () -> this.recipeService.getDetailedRecipe(-100L));
    }

    @Test
    public void createValidRecipe() throws Exception {
        ArrayList<RecipeIngredientDto> ingredients = new ArrayList<>();
        ingredients.add(new RecipeIngredientDto("Apple", 1f, null));

        // adding picture
        byte[] pic = readJpegFile(DEFAULT_PICTURE_FOLDER + "/1.jpg");

        RecipeDetailsDto newRecipe = new RecipeDetailsDto(-1L, "Eine Prise Test", "egal", "Beschreibung",
            ingredients, new ArrayList<>(), null, pic);

        this.recipeService.createRecipe(newRecipe, user.getId());

        RecipeSearchResultDto recipeResult = this.recipeService.searchRecipes(new RecipeSearchDto("", "Eine Prise Test", 0, 21));

        // asserting test
        assertNotNull(recipeResult);

        // check that it exists in the list
        assertThat(recipeResult.recipes())
            .extracting(RecipeListDto::name)
            .contains(
                "Eine Prise Test"
            );

        Recipe recipe = this.recipeRepository.findByNameContainingIgnoreCase("Eine Prise Test").get(0);
        this.recipeIngredientRepository.deleteAll(this.recipeIngredientRepository.findByRecipe(recipe));
        this.recipeRepository.delete(this.recipeRepository.findByNameContainingIgnoreCase("Eine Prise Test").get(0));
    }

    @Test
    public void createInValidRecipeNoImage() {
        ArrayList<RecipeIngredientDto> ingredients = new ArrayList<>();
        ingredients.add(new RecipeIngredientDto("Apple", 1f, null));

        // adding picture
        byte[] pic = null;

        RecipeDetailsDto newRecipe = new RecipeDetailsDto(-1L, "Eine Prise Test", "egal", "Beschreibung",
            ingredients, new ArrayList<>(), null, pic);

        assertThatExceptionOfType(ValidationException.class).isThrownBy(
            () -> this.recipeService.createRecipe(newRecipe, user.getId())
        );
    }

    @Test
    public void createInvalidRecipe() throws Exception {
        ArrayList<RecipeIngredientDto> ingredients = new ArrayList<>();
        ingredients.add(new RecipeIngredientDto("Chhhhhhhhhh", 1f, null));

        // adding picture
        byte[] pic = readJpegFile(DEFAULT_PICTURE_FOLDER + "/1.jpg");

        RecipeDetailsDto newRecipe = new RecipeDetailsDto(-1L, "Eine Prise Test", "egal", "Beschreibung",
            ingredients, new ArrayList<>(), null, pic);

        assertThatExceptionOfType(ConflictException.class).isThrownBy(
            () -> this.recipeService.createRecipe(newRecipe, 1L)
        );

        RecipeDetailsDto finalNewRecipe = new RecipeDetailsDto(-1L, "Eine Prise Test", "egal", "Beschreibung",
            new ArrayList<>(), new ArrayList<>(), null, null);

        assertThatExceptionOfType(ValidationException.class).isThrownBy(
            () -> this.recipeService.createRecipe(finalNewRecipe, 1L)
        );
    }
}

