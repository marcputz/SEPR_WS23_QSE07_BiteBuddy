package at.ac.tuwien.sepr.groupphase.backend.unittests.service;

import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.LoginDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.DataStoreException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.*;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthenticationService;
import at.ac.tuwien.sepr.groupphase.backend.service.MenuPlanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class MenuPlanServiceTest {

    @Autowired
    MenuPlanService service;

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

    @Test
    void testCreateMenuPlan_Returns() throws Exception {
        MenuPlan saveReturn = service.createEmptyMenuPlan(user, profile, LocalDate.now().minusDays(6), LocalDate.now());

        final MenuPlan plan = service.getById(saveReturn.getId());

        assertAll(
            () -> assertEquals(user.getId(), plan.getUser().getId()),
            () -> assertEquals(profile.getId(), plan.getProfile().getId()),
            () -> assertEquals(LocalDate.now().minusDays(6), plan.getFromDate()),
            () -> assertEquals(LocalDate.now(), plan.getUntilDate())
        );
    }

    @Test
    void testCreateMenuPlan_WithInvalidTimeframe_ThrowsValidationError() throws Exception {
        assertThrows(ValidationException.class, () -> service.createEmptyMenuPlan(user, profile, LocalDate.now(), LocalDate.now().minusDays(1)));
    }

    @Test
    void testCreateMenuPlan_WithInvalidUser_ThrowsDataStoreError() throws Exception {
        ApplicationUser u = new ApplicationUser();
        u.setId(Long.MAX_VALUE);
        u.setNickname("This user is never saved");
        u.setPasswordEncoded("test");
        u.setEmail("test@test");

        assertThrows(DataStoreException.class, () -> service.createEmptyMenuPlan(u, profile, LocalDate.now().minusDays(6), LocalDate.now()));
    }

    @Test
    void testCreateMenuPlan_WithInvalidProfile_ThrowsDataStoreError() throws Exception {
        Profile p = new Profile();
        p.setName("This profile is never saved");
        p.setUser(this.user);
        p.setId(Long.MAX_VALUE);

        assertThrows(DataStoreException.class, () -> service.createEmptyMenuPlan(user, p, LocalDate.now().minusDays(6), LocalDate.now()));
    }

    @Test
    void testCreateMenuPlan_WhenAlreadyExist_ThrowsConflictError() throws Exception {
        service.createEmptyMenuPlan(user, profile, LocalDate.now().minusDays(6), LocalDate.now());

        assertThrows(ConflictException.class, () -> service.createEmptyMenuPlan(user, profile, LocalDate.now().minusDays(6), LocalDate.now()));
    }

    @Test
    void testGenerateMenuPlan_WithValidData_Returns() throws Exception {
        MenuPlan plan = service.createEmptyMenuPlan(user, profile, LocalDate.now().minusDays(6), LocalDate.now());

        service.generateContent(plan);

        final MenuPlan result = service.getById(plan.getId());

        assertNotNull(result.getContent());
        assertNotEquals(0, result.getContent().size());
    }

    @Test
    void testGenerateMenuPlan_WithNotEnoughRecipes_ThrowsConflictError() throws Exception {
        this.recipeIngredientRepository.deleteAll();
        this.recipeIngredientDetailsRepository.deleteAll();
        this.recipeRepository.deleteAll();

        MenuPlan plan = service.createEmptyMenuPlan(user, profile, LocalDate.now().minusDays(6), LocalDate.now());

        assertThrows(ConflictException.class, () -> service.generateContent(plan));
    }

}
