package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.LoginDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan.MenuPlanCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan.MenuPlanDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.repository.*;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class MenuPlanEndpointTest {

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    @Autowired
    private RecipeRepository recipeRepository;

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
    private ProfileRepository profileRepository;

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private MenuPlanRepository menuPlanRepository;

    private ApplicationUser user;
    private Profile profile;

    @BeforeEach
    void setupMapper() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @BeforeEach
    public void setupRecipes() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();

        // creating ingredients
        // adding apple
        Ingredient ingredient1 = new Ingredient();
        ingredient1.setName("Apple");
        Ingredient i1 = ingredientRepository.save(ingredient1);

        // adding rice
        Ingredient ingredient2 = new Ingredient();
        ingredient2.setName("Rice");
        Ingredient i2 = ingredientRepository.save(ingredient2);

        // adding sugar
        Ingredient ingredient3 = new Ingredient();
        ingredient3.setName("sugar");
        Ingredient i3 = ingredientRepository.save(ingredient3);

        // creating recipes without ingredients

        int recipeAmount = 50;
        List<Recipe> recipes = new ArrayList<>();
        for (int i = 0; i < recipeAmount; i++) {
            Recipe r = new Recipe();
            r.setName("Recipe " + i);
            r.setInstructions("Recipe Instructions " + i);
            recipes.add(recipeRepository.save(r));
        }

        // creating recipeIngredientDetails

        List<RecipeIngredientDetails> details = new ArrayList<>();
        for (Recipe r : recipes) {
            RecipeIngredientDetails rid = new RecipeIngredientDetails();
            rid.setDescriber("some");
            rid.setAmount(1);
            rid.setUnit(FoodUnit.tablespoon);
            rid.setIngredient("thing");
            details.add(recipeIngredientDetailsRepository.save(rid));
        }

        // creating recipeIngredients

        for (int i = 0; i < recipes.size(); i++) {
            Recipe r = recipes.get(i);

            RecipeIngredient ri = new RecipeIngredient();
            ri.setRecipe(r);
            ri.setAmount(details.get(i));
            if (i % 3 == 0) {
                ri.setIngredient(i1);
            } else if (i % 3 == 1) {
                ri.setIngredient(i2);
            } else {
                ri.setIngredient(i3);
            }
            this.recipeIngredientRepository.save(ri);

            r.setIngredients(new HashSet<>(List.of(ri)));
            this.recipeRepository.save(r);
        }

        // allergens

        Allergene allergene1 = new Allergene();
        allergene1.setName("Fructose");
        Set<AllergeneIngredient> aIngredients = new HashSet<>();

        AllergeneIngredient allergeneIngredient1 = new AllergeneIngredient();
        allergeneIngredient1.setAllergene(allergene1);
        allergeneIngredient1.setIngredient(ingredient1);

        aIngredients.add(allergeneIngredient1);
        allergene1.setIngredients(aIngredients);
        allergeneIngredient1.setIngredient(ingredient1);

        allergeneRepository.save(allergene1);
        allergeneIngredientRepository.save(allergeneIngredient1);
    }

    @BeforeEach
    public void setupUsers() {
        ApplicationUser user = new ApplicationUser();
        user.setId(1L);
        user.setNickname("testuser");
        user.setEmail("test@test");
        user.setPasswordEncoded(PasswordEncoder.encode("password", "test@test"));

        this.user = this.userRepository.save(user);

        Profile profile = new Profile();
        profile.setName("Testprofil 1");
        profile.setUser(this.user);

        this.profile = this.profileRepository.save(profile);
    }

    @AfterEach
    public void afterEach() {
        menuPlanRepository.deleteAll();

        profileRepository.deleteAll();
        userRepository.deleteAll();

        recipeIngredientRepository.deleteAll();
        recipeIngredientDetailsRepository.deleteAll();
        allergeneIngredientRepository.deleteAll();
        ingredientRepository.deleteAll();
        recipeRepository.deleteAll();
    }

    public String authenticate() throws Exception {
        LoginDto dto = new LoginDto();
        dto.setEmail("test@test");
        dto.setPassword("password");
        return this.authService.loginUser(dto);
    }

    @Test
    void testGenerateMenuPlan_WhenLoggedIn_WithValidData_ReturnsOK() throws Exception {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.set("Authorization", authenticate());

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/menuplan/generate")
                .content(this.objectMapper.writeValueAsString(
                    new MenuPlanCreateDto()
                        .setFromTime(LocalDate.now().minusDays(6))
                        .setUntilTime(LocalDate.now())
                        .setProfileId(this.profile.getId())
                        .setFridge(List.of())
                ))
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        //MenuPlanDetailDto responseDto = objectMapper.readerFor(MenuPlanDetailDto.class).readValue(response.getContentAsByteArray());

        //System.out.println(responseDto);
    }

    @Test
    void testGenerateMenuPlan_WhenNotLoggedIn_ReturnsUnauthorized() throws Exception {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/menuplan/generate")
                .content(this.objectMapper.writeValueAsString(
                    new MenuPlanCreateDto()
                        .setFromTime(LocalDate.now().minusDays(6))
                        .setUntilTime(LocalDate.now())
                        .setProfileId(this.profile.getId())
                        .setFridge(List.of())
                ))
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
    }

    @Test
    void testGenerateMenuPlan_WhenLoggedIn_WithInvalidData_WrongTimeframe_ReturnsValidationError() throws Exception {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.set("Authorization", authenticate());

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/menuplan/generate")
                .content(this.objectMapper.writeValueAsString(
                    new MenuPlanCreateDto()
                        .setFromTime(LocalDate.now())
                        .setUntilTime(LocalDate.now().minusDays(6))
                        .setProfileId(this.profile.getId())
                        .setFridge(List.of())
                ))
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
    }

    @Test
    void testGenerateMenuPlan_WhenLoggedIn_WhenAlreadyExists_ReturnsConflictError() throws Exception {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.set("Authorization", authenticate());

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/menuplan/generate")
                .content(this.objectMapper.writeValueAsString(
                    new MenuPlanCreateDto()
                        .setFromTime(LocalDate.now().minusDays(6))
                        .setUntilTime(LocalDate.now())
                        .setProfileId(this.profile.getId())
                        .setFridge(List.of())
                ))
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        // second request
        mvcResult = this.mockMvc.perform(post("/api/v1/menuplan/generate")
                .content(this.objectMapper.writeValueAsString(
                    new MenuPlanCreateDto()
                        .setFromTime(LocalDate.now().minusDays(6))
                        .setUntilTime(LocalDate.now())
                        .setProfileId(this.profile.getId())
                        .setFridge(List.of())
                ))
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        response = mvcResult.getResponse();

        assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
    }
}
