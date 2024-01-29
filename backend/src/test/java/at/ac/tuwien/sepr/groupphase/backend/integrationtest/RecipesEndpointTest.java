package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.authentication.LoginDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe.RecipeDetailsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe.RecipeIngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe.RecipeListDto;
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
import at.ac.tuwien.sepr.groupphase.backend.repository.AllergeneIngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.AllergeneRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.IngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PictureRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeIngredientDetailsRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeIngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EnableWebMvc
@WebAppConfiguration
@ActiveProfiles("generateData")
public class RecipesEndpointTest {
    private static final String DEFAULT_PICTURE_FOLDER = (new File("")).getAbsolutePath() + "/src/main/resources/RecipePictures";

    @Autowired
    private WebApplicationContext webAppContext;
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private PictureRepository pictureRepository;

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
    private long testUserId;
    private String base64Picture = null;


    @BeforeEach
    public void setupUsers() {
        ApplicationUser user = new ApplicationUser();
        user.setId(1L);
        user.setNickname("testuser");
        user.setEmail("test@test");
        user.setPasswordEncoded(PasswordEncoder.encode("password", "test@test"));

        this.user = this.userRepository.save(user);
    }

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

    public static String convertToBase64(byte[] byteArray) {
        return Base64.getEncoder().encodeToString(byteArray);
    }

    @BeforeEach
    public void setup() throws IOException {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();

        // adding picture
        Picture pic = new Picture();
        pic.setDescription("keine");
        pic.setData(readJpegFile(DEFAULT_PICTURE_FOLDER + "/1.jpg"));
        this.pictureRepository.save(pic);

        this.base64Picture = Base64.getEncoder().encodeToString(readJpegFile(DEFAULT_PICTURE_FOLDER + "/1.jpg"));

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
        recipe1Id = recipeRepository.save(recipe1).getId();

        Recipe recipe2 = new Recipe();
        recipe2.setCreatorId(1L);
        recipe2.setInstructions("Instructions2");
        recipe2.setName("recipe 2");
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
    }

    @Test
    public void getAllAddedRecipes() throws Exception {
        // creating request
        var body = mockMvc
            .perform(MockMvcRequestBuilders
                .post("/api/v1/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                    "name": "recipe",
                    "creator": "",
                    "page": 0,
                    "entriesPerPage": 21
                    }
                    """)
                .accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();

        // mapping
        RecipeSearchResultDto recipeResult = objectMapper.readerFor(RecipeSearchResultDto.class).readValue(body);

        // asserting test
        assertNotNull(recipeResult);

        assertThat(recipeResult.recipes())
            .extracting(RecipeListDto::id, RecipeListDto::name, RecipeListDto::creator)
            .contains(
                tuple(recipe1Id, "recipe 1", "BiteBuddy"),
                tuple(recipe2Id, "recipe 2", "testuser")
            );
    }

    @Test
    public void getFilteredRecipe() throws Exception {
        // creating request
        var body = mockMvc
            .perform(MockMvcRequestBuilders
                .post("/api/v1/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                    "name": "1",
                    "creator": "",
                    "page": 0,
                    "entriesPerPage": 1000
                    }
                    """)
                .accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();

        // mapping
        RecipeSearchResultDto recipeResult = objectMapper.readerFor(RecipeSearchResultDto.class).readValue(body);

        // asserting test
        assertNotNull(recipeResult);

        assertThat(recipeResult.recipes())
            .extracting(RecipeListDto::id, RecipeListDto::name, RecipeListDto::creator)
            .contains(
                tuple(recipe1Id, "recipe 1", "BiteBuddy")
            );
    }

    @Test
    public void getRecipeDetailsOfExistingRecipe() throws Exception {
        // creating request
        var body = mockMvc
            .perform(MockMvcRequestBuilders
                .get("/api/v1/recipes/" + recipe1Id)
                .accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();

        // mapping
        RecipeDetailsDto recipeDetails = objectMapper.readerFor(RecipeDetailsDto.class).readValue(body);

        // asserting test
        assertNotNull(recipeDetails);

        assertAll(
            () -> assertThat(recipeDetails.name())
                .contains(
                    "recipe 1"
                ),
            () -> assertThat(recipeDetails.description())
                .contains(
                    "Instructions 1"
                ),
            () -> assertThat(recipeDetails.ingredients())
                .hasSize(1)
                .contains(
                    new RecipeIngredientDto("sugar", 1.0f, FoodUnit.tablespoon)
                ),
            () -> assertThat(recipeDetails.allergens())
                .hasSize(1)
                .contains("Fructose")
        );
    }

    @Test
    public void getRecipeDetailsOfNonExistingRecipeRespondsNotFound() throws Exception {
        // creating request
        var body = mockMvc
            .perform(MockMvcRequestBuilders
                .get("/api/v1/recipes/" + -100)
                .accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().isNotFound())
            .andReturn().getResponse().getContentAsByteArray();

    }

    @Test
    public void createValidRecipe() throws Exception {
        // creating request
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.set("Authorization", authenticate());

        String jsonPayload = """
            {
                "id": -1,
                "name": "Eine Prise Test",
                "description": "Man nehme einen Test",
                "ingredients": [
                    {
                        "name": "Apple",
                        "amount": 1,
                        "unit": "pound"
                    }
                ],
                "allergens": [],
                "picture": "%s"
            }
            """.formatted(base64Picture);

        mockMvc
            .perform(MockMvcRequestBuilders
                .post("/api/v1/recipes/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload)
                .headers(requestHeaders)
                .accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().isCreated());

        // now requesting the recipe
        // creating request
        var body = mockMvc
            .perform(MockMvcRequestBuilders
                .post("/api/v1/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                    "name": "Eine Prise Test",
                    "creator": "",
                    "page": 0,
                    "entriesPerPage": 1000
                    }
                    """)
                .accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();

        // mapping
        RecipeSearchResultDto recipeResult = objectMapper.readerFor(RecipeSearchResultDto.class).readValue(body);

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
    public void createRecipeWithoutLogin() throws Exception {
        // creating request
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.set("Authorization", "nichts");

        String jsonPayload = """
            {
            "id": -1,
            "name": "Eine Prise Test",
            "description": "Man nehme einen Test",
            "ingredients": [
                {
                    "name": "Apple",
                    "amount": 1,
                    "unit": "pound"
                }
            ],
            "allergens": [],
            "picture": "%s"
            }
            """.formatted(this.base64Picture);

        mockMvc
            .perform(MockMvcRequestBuilders
                .post("/api/v1/recipes/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload)
                .headers(requestHeaders)
                .accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().isUnauthorized());
    }

    @Test
    public void createInvalidRecipe() throws Exception {
        // creating request
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.set("Authorization", authenticate());

        String jsonPayload = """
                    {
                    "id": -1,
                    "name": "Eine Prise Test",
                    "description": "Man nehme einen Test",
                    "ingredients": [
                        {
                            "name": "Appsadasdasdle",
                            "amount": 1,
                            "unit": "pound"
                        }
                    ],
                    "allergens": [],
                    "picture": "%s"
                    }
                    """.formatted(this.base64Picture);

        mockMvc
            .perform(MockMvcRequestBuilders
                .post("/api/v1/recipes/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload)
                .headers(requestHeaders)
                .accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().isConflict());

        // creating request with no ingredient
        mockMvc
            .perform(MockMvcRequestBuilders
                .post("/api/v1/recipes/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                    "name": "Eine Prise falscher Test",
                    "description": "Man nehme einen Test 1313üaääw",
                    "ingredients": [],
                    "picture": "%s"
                    }
                    """.formatted(base64Picture))
                .accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().is4xxClientError());

        // creating request with too long name
        mockMvc
            .perform(MockMvcRequestBuilders
                .post("/api/v1/recipes/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                    "name": "Eine Prise Testwefffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
                    "description": "Man nehme einen Test 1313üaääw",
                    "ingredients": [],
                    "picture": "%s"
                    }
                    """.formatted(base64Picture))
                .accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().is4xxClientError());

        // creating request without picture
        jsonPayload = """
                    {
                    "id": -1,
                    "name": "Eine Prise Test",
                    "description": "Man nehme einen Test",
                    "ingredients": [
                        {
                            "name": "Appsadasdasdle",
                            "amount": 1,
                            "unit": "pound"
                        }
                    ],
                    "allergens": [],
                    "picture": ""
                    }
                    """;
        mockMvc
            .perform(MockMvcRequestBuilders
                .post("/api/v1/recipes/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload)
                .headers(requestHeaders)
                .accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().is4xxClientError());
    }
}

