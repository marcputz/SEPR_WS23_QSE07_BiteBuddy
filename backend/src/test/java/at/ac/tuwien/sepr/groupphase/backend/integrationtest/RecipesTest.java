package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeListDto;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;

import static org.assertj.core.api.Assertions.tuple;
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

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();
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
                    "creator": ""
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
            .hasSize(3)
            .extracting(RecipeListDto::id, RecipeListDto::name, RecipeListDto::creator)
            .contains(
                tuple(1L, "Thai Spicy Basil Chicken Fried Rice", null),
                tuple(2L, "Authentic Paella Valenciana", null),
                tuple(3L, "Petit Trois's French Onion Soup", null)
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
                    "name": "rice",
                    "creator": ""
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
            .hasSize(1)
            .extracting(RecipeListDto::id, RecipeListDto::name, RecipeListDto::creator)
            .contains(
                tuple(1L, "Thai Spicy Basil Chicken Fried Rice", null)
            );
    }
}

