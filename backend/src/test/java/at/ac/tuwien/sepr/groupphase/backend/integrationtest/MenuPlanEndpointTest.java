package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InventoryIngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InventoryListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.authentication.LoginDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan.MenuPlanCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan.MenuPlanDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Allergene;
import at.ac.tuwien.sepr.groupphase.backend.entity.AllergeneIngredient;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.FoodUnit;
import at.ac.tuwien.sepr.groupphase.backend.entity.Ingredient;
import at.ac.tuwien.sepr.groupphase.backend.entity.Picture;
import at.ac.tuwien.sepr.groupphase.backend.entity.Profile;
import at.ac.tuwien.sepr.groupphase.backend.entity.Recipe;
import at.ac.tuwien.sepr.groupphase.backend.entity.RecipeIngredient;
import at.ac.tuwien.sepr.groupphase.backend.entity.RecipeIngredientDetails;
import at.ac.tuwien.sepr.groupphase.backend.repository.AllergeneIngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.AllergeneRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.IngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.MenuPlanRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PictureRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ProfileRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeIngredientDetailsRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeIngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class MenuPlanEndpointTest {
    private static final String DEFAULT_PICTURE_FOLDER = (new File("")).getAbsolutePath() + "/src/main/resources/RecipePictures";

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

    @Autowired
    private PictureRepository pictureRepository;

    private Profile profile;
    private ApplicationUser user;

    public static byte[] readJpegFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readAllBytes(path);
    }

    @BeforeEach
    void setupMapper() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @BeforeEach
    public void setupRecipes() throws IOException {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();

        // adding picture
        Picture pic = new Picture();
        pic.setDescription("keine");
        pic.setData(readJpegFile(DEFAULT_PICTURE_FOLDER + "/1.jpg"));
        this.pictureRepository.save(pic);

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
            r.setPictureId(1L);
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

        ApplicationUser user = new ApplicationUser()
            .setId(1L)
            .setNickname("testuser")
            .setEmail("test@test")
            .setPasswordEncoded(PasswordEncoder.encode("password", "test@test"));

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
        dto.setEmail(this.user.getEmail());
        dto.setPassword("password");
        return this.authService.loginUser(dto);
    }

    @Test
    void testGenerateMenuPlan_WhenLoggedIn_WithValidData_ReturnsOKAndValidDto() throws Exception {
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

        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        MenuPlanDetailDto responseDto = objectMapper.readerFor(MenuPlanDetailDto.class).readValue(response.getContentAsByteArray());

        assertAll(
            () -> assertEquals(LocalDate.now().minusDays(6), responseDto.getFromTime()),
            () -> assertEquals(LocalDate.now(), responseDto.getUntilTime()),
            () -> assertEquals(this.profile.getId(), responseDto.getProfileId())
        );
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
    }

    @Test
    void testGenerateMenuPlan_WhenLoggedIn_WithNoData_ReturnsBadRequest() throws Exception {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.set("Authorization", authenticate());

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/menuplan/generate")
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
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

        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
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

    @Test
    void generateMenuPlanWhenLoggedInWithValidFridge() throws Exception {
        // creating menuPlan
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
                        .setFridge(List.of("thing", "Apple"))
                ))
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
    }

    @Test
    void generateMenuPlanWhenLoggedInWithInValidFridgeThrowsException() throws Exception {
        // creating menuPlan
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
                        .setFridge(List.of("thing", "Apppppple"))
                ))
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
    }

    @Test
    void testGettingNormalInventory() throws Exception {
        // creating menuPlan
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
                        .setFridge(List.of("thing", "Apple"))
                ))
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        // getting inventory
        var body = mockMvc
            .perform(MockMvcRequestBuilders
                .get("/api/v1/menuplan/inventory/")
                .headers(requestHeaders)
                .accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();

        InventoryListDto inventory = objectMapper.readerFor(InventoryListDto.class).readValue(body);

        // asserting test
        assertNotNull(inventory);

        assertThat(inventory.available())
            .extracting(InventoryIngredientDto::getName, InventoryIngredientDto::isInventoryStatus)
            .contains(
                tuple("Apple", true),
                tuple("thing", true)
            );
    }

    @Test
    void testGettingEmptyAvailableInventory() throws Exception {
        // creating menuPlan
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
        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        // getting inventory
        var body = mockMvc
            .perform(MockMvcRequestBuilders
                .get("/api/v1/menuplan/inventory/")
                .headers(requestHeaders)
                .accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();

        InventoryListDto inventory = objectMapper.readerFor(InventoryListDto.class).readValue(body);

        // asserting test
        assertNotNull(inventory);
        assertEquals(0, inventory.available().size());
        assertFalse(inventory.missing().isEmpty());
    }

    @Test
    void testGetInventoryWithoutAuthenticationThrowsException() throws Exception {
        // creating menuPlan
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
        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        // getting inventory
        mvcResult = this.mockMvc.perform(get("/api/v1/menuplan/inventory/"))
            .andDo(print())
            .andReturn();

        response = mvcResult.getResponse();
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }

    @Test
    void testUpdateInventoryIngredientValid() throws Exception {
        // creating menuPlan
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
        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        // getting inventory
        var body = mockMvc
            .perform(MockMvcRequestBuilders
                .get("/api/v1/menuplan/inventory/")
                .headers(requestHeaders)
                .accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();

        InventoryListDto inventory = objectMapper.readerFor(InventoryListDto.class).readValue(body);

        // updating inventory
        mvcResult = this.mockMvc.perform(put("/api/v1/menuplan/inventory/update")
            .content(this.objectMapper.writeValueAsString(
                    inventory.missing().get(0).setInventoryStatus(true)
                ))
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        response = mvcResult.getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Test
    void testUpdateInventoryIngredientInValid() throws Exception {
        // creating menuPlan
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
        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        // getting inventory
        var body = mockMvc
            .perform(MockMvcRequestBuilders
                .get("/api/v1/menuplan/inventory/")
                .headers(requestHeaders)
                .accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();

        InventoryListDto inventory = objectMapper.readerFor(InventoryListDto.class).readValue(body);

        // updating inventory
        mvcResult = this.mockMvc.perform(put("/api/v1/menuplan/inventory/update")
                .content(this.objectMapper.writeValueAsString(
                    inventory.missing().get(0).setIngredientId(55).setDetailedName("Test")
                ))
                .headers(requestHeaders))
            .andDo(print())
            .andReturn();
        response = mvcResult.getResponse();
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatus());
    }

    @Test
    void testUpdateInventoryIngredientNotAuthenticated() throws Exception {
        // creating menuPlan
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
        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        // getting inventory
        var body = mockMvc
            .perform(MockMvcRequestBuilders
                .get("/api/v1/menuplan/inventory/")
                .headers(requestHeaders)
                .accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();

        InventoryListDto inventory = objectMapper.readerFor(InventoryListDto.class).readValue(body);

        requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        // updating inventory
        mvcResult = this.mockMvc.perform(put("/api/v1/menuplan/inventory/update")
                .content(this.objectMapper.writeValueAsString(
                    inventory.missing().get(0).setInventoryStatus(true)
                )).headers(requestHeaders)
                )
            .andDo(print())
            .andReturn();
        response = mvcResult.getResponse();
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }
}
