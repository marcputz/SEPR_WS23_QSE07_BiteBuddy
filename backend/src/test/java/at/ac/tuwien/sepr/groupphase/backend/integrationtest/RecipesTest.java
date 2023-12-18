package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeDetailsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeListDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EnableWebMvc
@WebAppConfiguration
@ActiveProfiles("generateData")
public class RecipesTest {
    @Autowired
    private WebApplicationContext webAppContext;
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private RecipeIngredientRepository recipeIngredientRepository;

    @Autowired
    private AllergeneRepository allergeneRepository;

    @Autowired
    private AllergeneIngredientRepository allergeneIngredientRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    private long recipe1Id;
    private long recipeIngredient1Id;
    private long allergene1Id;
    private long ingredient1Id;
    private long allergeneIngredient1Id;

    private long recipe2Id;
    private long recipeIngredient2Id;
    private long ingredient2Id;



    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();

        Allergene allergene1 = new Allergene();
        allergene1.setName("Fructose");
        Set<AllergeneIngredient> aIngredients = new HashSet<>();
        AllergeneIngredient allergeneIngredient1 = new AllergeneIngredient();
        allergeneIngredient1.setAllergene(allergene1);

        Ingredient ingredient1 = new Ingredient();
        ingredient1.setName("Apple");
        RecipeIngredient recipeIngredient1 = new RecipeIngredient();
        recipeIngredient1.setIngredient(ingredient1);
        recipeIngredient1.setAmount("100 g");

        Ingredient ingredient2 = new Ingredient();
        ingredient2.setName("Rice");
        RecipeIngredient recipeIngredient2 = new RecipeIngredient();
        recipeIngredient2.setIngredient(ingredient2);
        recipeIngredient2.setAmount("200 g");

        Recipe recipe1  = new Recipe();
        recipe1.setInstructions("Instructions1");
        recipe1.setName("recipe 1");
        Set<RecipeIngredient> rIngredients1 = new HashSet<>();
        rIngredients1.add(recipeIngredient1);
        rIngredients1.add(recipeIngredient2);
        recipe1.setIngredients(rIngredients1);
        recipeIngredient1.setRecipe(recipe1);
        recipeIngredient2.setRecipe(recipe1);
        aIngredients.add(allergeneIngredient1);
        allergene1.setIngredients(aIngredients);
        allergeneIngredient1.setIngredient(ingredient1);

        Recipe recipe2  = new Recipe();
        recipe2.setInstructions("Instructions2");
        recipe2.setName("recipe 2");
        Set<RecipeIngredient> rIngredients2 = new HashSet<>();
        rIngredients2.add(recipeIngredient2);
        recipe2.setIngredients(rIngredients2);

        allergene1Id = allergeneRepository.save(allergene1).getId();
        ingredient1Id = ingredientRepository.save(ingredient1).getId();
        ingredient2Id  = ingredientRepository.save(ingredient2).getId();
        recipe1Id = recipeRepository.save(recipe1).getId();
        recipe2Id = recipeRepository.save(recipe2).getId();
        allergeneIngredient1Id = allergeneIngredientRepository.save(allergeneIngredient1).getId();
        recipeIngredient1Id = recipeIngredientRepository.save(recipeIngredient1).getId();
        recipeIngredient2Id = recipeIngredientRepository.save(recipeIngredient2).getId();

    }

    @AfterEach
    public void afterEach() {
        recipeIngredientRepository.deleteById(recipeIngredient1Id);
        recipeIngredientRepository.deleteById(recipeIngredient2Id);
        allergeneIngredientRepository.deleteById(allergeneIngredient1Id);
        allergeneRepository.deleteById(allergene1Id);
        ingredientRepository.deleteById(ingredient1Id);
        ingredientRepository.deleteById(ingredient2Id);
        recipeRepository.deleteById(recipe1Id);
        recipeRepository.deleteById(recipe2Id);
    }

    @Test
    public void getAllRecipes() throws Exception {
        // creating request
        var body = mockMvc
            .perform(MockMvcRequestBuilders
                .post("/api/v1/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                    "name": "",
                    "creator": "",
                    "page": 0,
                    "entriesPerPage": 1000
                    }
                    """)
                .accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();

        // mapping
        List<RecipeListDto> recipeResult = objectMapper.readerFor(RecipeListDto.class).<RecipeListDto>readValues(body).readAll();

        // asserting test
        assertNotNull(recipeResult);

        assertThat(recipeResult)
            .extracting(RecipeListDto::id, RecipeListDto::name, RecipeListDto::creator)
            .contains(
                tuple(recipe1Id, "recipe 1", null),
                tuple(recipe2Id, "recipe 2", null)
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
        List<RecipeListDto> recipeResult = objectMapper.readerFor(RecipeListDto.class).<RecipeListDto>readValues(body).readAll();

        // asserting test
        assertNotNull(recipeResult);

        assertThat(recipeResult)
            .extracting(RecipeListDto::id, RecipeListDto::name, RecipeListDto::creator)
            .contains(
                tuple(recipe1Id, "recipe 1", null)
            );
    }

    @Test
    public void getRecipeDetails() throws Exception {
        // creating request
        var body = mockMvc
            .perform(MockMvcRequestBuilders
                .get("/api/v1/recipes/" + recipe1Id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                    "name": "",
                    "description": "",
                    "ingredients": "",
                    "allergens": "",
                    "picture": ""
                    }
                    """)
                .accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();

        // mapping
        List<RecipeDetailsDto> recipeDetails = objectMapper.readerFor(RecipeDetailsDto.class).<RecipeDetailsDto>readValues(body).readAll();

        // asserting test
        assertNotNull(recipeDetails.get(0));

        assertAll(
            () -> assertThat(recipeDetails.get(0).name())
                .contains(
                    "recipe 1"
                ),
            () -> assertThat(recipeDetails.get(0).description())
            .contains(
                "Instructions1"
            ),
            () -> assertThat(recipeDetails.get(0).ingredients())
            .hasSize(2)
            .contains(
                "Apple: 100 g", "Rice: 200 g"
            ),
            () -> assertThat(recipeDetails.get(0).allergens())
            .hasSize(1)
            .contains("Fructose")
        );

    }
}

