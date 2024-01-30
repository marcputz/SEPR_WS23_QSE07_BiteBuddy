package at.ac.tuwien.sepr.groupphase.backend.unittests.service;

import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InventoryIngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InventoryListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeRatingDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeRatingListsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan.MenuPlanUpdateRecipeDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.DataStoreException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.AllergeneIngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.AllergeneRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.IngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.MenuPlanRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ProfileRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeIngredientDetailsRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeIngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthenticationService;
import at.ac.tuwien.sepr.groupphase.backend.service.MenuPlanService;
import at.ac.tuwien.sepr.groupphase.backend.service.ProfileService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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
    private ProfileService profileService;


    @Autowired
    private AuthenticationService authService;

    @Autowired
    private MenuPlanRepository menuPlanRepository;

    private ApplicationUser user;
    private Profile profile;

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

        user.setActiveProfile(profile);
        this.profile = this.profileRepository.save(profile);
        this.user = this.userRepository.save(user);

    }

    @AfterEach
    public void afterEach() {
        this.user = this.user.setActiveProfile(null);
        this.user = this.userRepository.save(user);

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
    void testCreateMenuPlan_WithInvalidTimeframe_ThrowsValidationError() {
        assertThrows(ValidationException.class, () -> service.createEmptyMenuPlan(user, profile, LocalDate.now(), LocalDate.now().minusDays(1)));
    }

    @Test
    void testCreateMenuPlan_WithInvalidUser_ThrowsDataStoreError() {
        ApplicationUser u = new ApplicationUser();
        u.setId(Long.MAX_VALUE);
        u.setNickname("This user is never saved");
        u.setPasswordEncoded("test");
        u.setEmail("test@test");

        assertThrows(DataStoreException.class, () -> service.createEmptyMenuPlan(u, profile, LocalDate.now().minusDays(6), LocalDate.now()));
    }

    @Test
    void testCreateMenuPlan_WithInvalidProfile_ThrowsDataStoreError() {
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

    @Test
    void testUpdateMenuPlanByChangingOneRecipe() throws Exception {
        user.setActiveProfile(profile);
        MenuPlan saveReturn = service.createEmptyMenuPlan(user, profile, LocalDate.now().minusDays(6), LocalDate.now());
        service.generateContent(saveReturn);

        final MenuPlan plan = service.getById(saveReturn.getId());
        MenuPlanContent toChange = new MenuPlanContent();
        for (MenuPlanContent content : plan.getContent()) {
            if (content.getDayIdx() == 1 && content.getTimeslot() == 1) {
                toChange = content;
                break;
            }
        }

        MenuPlanUpdateRecipeDto menuPlanUpdateRecipeDto = new MenuPlanUpdateRecipeDto();
        menuPlanUpdateRecipeDto.setMenuPlanId(Math.toIntExact(plan.getId()));
        menuPlanUpdateRecipeDto.setDay(toChange.getDayIdx());
        menuPlanUpdateRecipeDto.setTimeslot(toChange.getTimeslot());
        menuPlanUpdateRecipeDto.setDislike(false);

        MenuPlan returnedMenuplan = service.updateMenuPlanByChangingOneRecipe(user, menuPlanUpdateRecipeDto);
        MenuPlanContent returnedContent = new MenuPlanContent();
        for (MenuPlanContent content : returnedMenuplan.getContent()) {
            if (content.getDayIdx() == 1 && content.getTimeslot() == 1) {
                returnedContent = content;
                break;
            }
        }

        MenuPlanContent finalToChange = toChange;
        MenuPlanContent finalReturnedContent = returnedContent;
        assertAll(
            () -> assertEquals(finalToChange.getDayIdx(), finalReturnedContent.getDayIdx()),
            () -> assertEquals(finalToChange.getTimeslot(), finalReturnedContent.getTimeslot()),
            () -> assertEquals(finalToChange.getMenuplan(), finalReturnedContent.getMenuplan()),
            () -> assertNotEquals(finalToChange.getRecipe(), finalReturnedContent.getRecipe())
        );
    }

    @Test
    void testUpdateMenuPlanByChangingOneRecipe_WithSetDislikeTrue_DislikesRecipeInProfile() throws Exception {
        user.setActiveProfile(profile);
        MenuPlan saveReturn = service.createEmptyMenuPlan(user, profile, LocalDate.now().minusDays(6), LocalDate.now());
        service.generateContent(saveReturn);

        final MenuPlan plan = service.getById(saveReturn.getId());
        MenuPlanContent toChange = new MenuPlanContent();
        Recipe myRecipe = new Recipe();
        for (MenuPlanContent content : plan.getContent()) {
            if (content.getDayIdx() == 1 && content.getTimeslot() == 1) {
                myRecipe = content.getRecipe();
                toChange = content;
                break;
            }
        }

        RecipeRatingDto ratingDto = new RecipeRatingDto(myRecipe.getId(), user.getId(), 1);
        profileService.rateRecipe(ratingDto);

        // re-roll recipe with disliking
        MenuPlanUpdateRecipeDto menuPlanUpdateRecipeDto = new MenuPlanUpdateRecipeDto();
        menuPlanUpdateRecipeDto.setMenuPlanId(Math.toIntExact(plan.getId()));
        menuPlanUpdateRecipeDto.setDay(toChange.getDayIdx());
        menuPlanUpdateRecipeDto.setTimeslot(toChange.getTimeslot());
        menuPlanUpdateRecipeDto.setDislike(true);

        service.updateMenuPlanByChangingOneRecipe(user, menuPlanUpdateRecipeDto);

        RecipeRatingListsDto returnedRating = profileService.getRatingLists(user.getId());
        List<Long> likes = returnedRating.likes();
        List<Long> dislikes = returnedRating.dislikes();
        Recipe finalMyRecipe = myRecipe;
        assertAll(
            () -> assertFalse(likes.contains(finalMyRecipe.getId())),
            () -> assertTrue(dislikes.contains(finalMyRecipe.getId()))
        );
    }

    @Test
    void testUpdateMenuPlanByChangingOneRecipe_withWrongDayIndex_throwsValidationException() throws Exception {
        user.setActiveProfile(profile);
        MenuPlan saveReturn = service.createEmptyMenuPlan(user, profile, LocalDate.now().minusDays(6), LocalDate.now());
        service.generateContent(saveReturn);

        final MenuPlan plan = service.getById(saveReturn.getId());
        MenuPlanContent toChange = new MenuPlanContent();
        for (MenuPlanContent content : plan.getContent()) {
            if (content.getDayIdx() == 1 && content.getTimeslot() == 1) {
                toChange = content;
                break;
            }
        }

        MenuPlanUpdateRecipeDto menuPlanUpdateRecipeDto = new MenuPlanUpdateRecipeDto();
        menuPlanUpdateRecipeDto.setMenuPlanId(Math.toIntExact(plan.getId()));
        menuPlanUpdateRecipeDto.setDay(-1);
        menuPlanUpdateRecipeDto.setTimeslot(toChange.getTimeslot());
        menuPlanUpdateRecipeDto.setDislike(false);

        assertThrows(ValidationException.class, () -> this.service.updateMenuPlanByChangingOneRecipe(user, menuPlanUpdateRecipeDto));
    }

    @Test
    void testCreateFridgeSuccessful() throws Exception {
        MenuPlan plan = service.createEmptyMenuPlan(user, profile, LocalDate.now().minusDays(6), LocalDate.now());

        // create fridge
        List<String> fridge = new ArrayList<>();
        fridge.add("Rice");
        fridge.add("thing");
        this.service.createFridge(plan, fridge);

        assertEquals(2, this.service.searchInventory(plan.getId()).available().size());
    }

    @Test
    void testCheckCorrectInventoryCombination() throws Exception {
        // Checks that if we have a base ingredient it marks everything as bought from detailed ingredients
        // Creating needed data for that
        Recipe r = new Recipe();
        r.setName("A bunch of chicken");
        r.setInstructions("Recipe Instructions");
        r = recipeRepository.save(r);

        // adding chicken
        var ing = new Ingredient();
        ing.setName("Chicken");
        ingredientRepository.save(ing);

        // adding rids
        Set<RecipeIngredient> ris = new HashSet<>();
        List<RecipeIngredientDetails> rids = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            RecipeIngredientDetails rid = new RecipeIngredientDetails();
            rid.setDescriber("some");
            rid.setAmount(1.5f);
            rid.setUnit(FoodUnit.ounce);
            rid.setIngredient("Chicken TESTED");
            rids.add(recipeIngredientDetailsRepository.save(rid));

            RecipeIngredient ri = new RecipeIngredient();
            ri.setIngredient(ing);
            ri.setAmount(rid);
            ri.setRecipe(r);
            recipeIngredientRepository.save(ri);
        }

        r.setIngredients(ris);
        recipeRepository.save(r);

        Recipe recipe = recipeRepository.findByNameContainingIgnoreCase("A bunch of chicken").get(0);

        // create fridge & menuPlan
        MenuPlan plan = service.createEmptyMenuPlan(user, profile, LocalDate.now().minusDays(6), LocalDate.now());
        List<String> fridge = new ArrayList<>();
        fridge.add("Chicken");
        this.service.createFridge(plan, fridge);

        // custom MenuPlanContent
        plan.addContent(new MenuPlanContent(plan, 1, 1, recipe));
        plan = this.menuPlanRepository.save(plan);

        this.service.createInventory(plan.getUser());

        InventoryListDto inventory = this.service.searchInventory(plan.getId());
        assertEquals(2, inventory.available().size());

        assertEquals(4.5f, inventory.available().get(1).getAmount());
    }

    @Test
    void testCheckCorrectInventoryChecking() throws Exception {
        // Checks that if we have a base ingredient it marks everything as bought from detailed ingredients
        // Creating needed data for that
        Recipe r = new Recipe();
        r.setName("A bunch of chicken");
        r.setInstructions("Recipe Instructions");
        r = recipeRepository.save(r);

        // adding chicken
        var ing = new Ingredient();
        ing.setName("Chicken");
        ingredientRepository.save(ing);

        // adding rids
        Set<RecipeIngredient> ris = new HashSet<>();
        List<RecipeIngredientDetails> rids = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            RecipeIngredientDetails rid = new RecipeIngredientDetails();
            rid.setDescriber("some");
            rid.setAmount(1.5f);
            rid.setUnit(FoodUnit.values()[i]);
            rid.setIngredient("Chicken TESTED");
            rids.add(recipeIngredientDetailsRepository.save(rid));

            RecipeIngredient ri = new RecipeIngredient();
            ri.setIngredient(ing);
            ri.setAmount(rid);
            ri.setRecipe(r);
            recipeIngredientRepository.save(ri);
        }

        r.setIngredients(ris);
        recipeRepository.save(r);

        Recipe recipe = recipeRepository.findByNameContainingIgnoreCase("A bunch of chicken").get(0);

        // create fridge & menuPlan
        MenuPlan plan = service.createEmptyMenuPlan(user, profile, LocalDate.now().minusDays(6), LocalDate.now());
        List<String> fridge = new ArrayList<>();
        fridge.add("Chicken");
        this.service.createFridge(plan, fridge);

        // custom MenuPlanContent
        plan.addContent(new MenuPlanContent(plan, 1, 1, recipe));
        plan = this.menuPlanRepository.save(plan);

        this.service.createInventory(plan.getUser());

        InventoryListDto inventory = this.service.searchInventory(plan.getId());
        assertEquals(4, inventory.available().size());
    }

    @Test
    void testCreateInventoryWithMissingAndAvailableIngredients() throws Exception {
        // Checks that if we have a base ingredient it marks everything as bought from detailed ingredients
        // Creating needed data for that
        Recipe r = new Recipe();
        r.setName("A bunch of chicken");
        r.setInstructions("Recipe Instructions");
        r = recipeRepository.save(r);

        // adding chicken
        var ing = new Ingredient();
        ing.setName("Chicken");
        ingredientRepository.save(ing);

        // adding rids
        Set<RecipeIngredient> ris = new HashSet<>();
        List<RecipeIngredientDetails> rids = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            RecipeIngredientDetails rid = new RecipeIngredientDetails();
            rid.setDescriber("some");
            rid.setAmount(1.5f);
            rid.setUnit(FoodUnit.ounce);
            rid.setIngredient("Chicken TESTED");
            rids.add(recipeIngredientDetailsRepository.save(rid));

            RecipeIngredient ri = new RecipeIngredient();
            ri.setIngredient(ing);
            ri.setAmount(rid);
            ri.setRecipe(r);
            recipeIngredientRepository.save(ri);
        }

        r.setIngredients(ris);
        recipeRepository.save(r);

        Recipe recipe = recipeRepository.findByNameContainingIgnoreCase("A bunch of chicken").get(0);

        // create fridge & menuPlan
        MenuPlan plan = service.createEmptyMenuPlan(user, profile, LocalDate.now().minusDays(6), LocalDate.now());
        List<String> fridge = new ArrayList<>();
        fridge.add("Chicken");
        this.service.createFridge(plan, fridge);

        // custom MenuPlanContent
        plan.addContent(new MenuPlanContent(plan, 1, 1, recipe));

        recipe = recipeRepository.findByNameContainingIgnoreCase("Recipe 1").get(0);
        plan.addContent(new MenuPlanContent(plan, 1, 2, recipe));
        plan = this.menuPlanRepository.save(plan);

        this.service.createInventory(plan.getUser());

        InventoryListDto inventory = this.service.searchInventory(plan.getId());
        assertEquals(2, inventory.available().size());
        assertEquals(1, inventory.missing().size());
        assertEquals(4.5f, inventory.available().get(1).getAmount());
    }

    @Test
    void testWrongIngredientThrowsException() throws Exception {
        MenuPlan plan = service.createEmptyMenuPlan(user, profile, LocalDate.now().minusDays(6), LocalDate.now());

        // create fridge
        List<String> fridge = new ArrayList<>();
        fridge.add("RiceASD");
        fridge.add("AThing");

        assertThrows(ValidationException.class, () -> this.service.createFridge(plan, fridge));
    }

    @Test
    void updateSuccessfulInventoryIngredient() throws Exception {
        // Creating Inventory
        Recipe r = new Recipe();
        r.setName("A bunch of chicken");
        r.setInstructions("Recipe Instructions");
        r = recipeRepository.save(r);

        // adding chicken
        var ing = new Ingredient();
        ing.setName("Chicken");
        ingredientRepository.save(ing);

        // adding rids
        Set<RecipeIngredient> ris = new HashSet<>();
        List<RecipeIngredientDetails> rids = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            RecipeIngredientDetails rid = new RecipeIngredientDetails();
            rid.setDescriber("some");
            rid.setAmount(1.5f);
            rid.setUnit(FoodUnit.ounce);
            rid.setIngredient("Chicken TESTED");
            rids.add(recipeIngredientDetailsRepository.save(rid));

            RecipeIngredient ri = new RecipeIngredient();
            ri.setIngredient(ing);
            ri.setAmount(rid);
            ri.setRecipe(r);
            recipeIngredientRepository.save(ri);
        }

        r.setIngredients(ris);
        recipeRepository.save(r);

        Recipe recipe = recipeRepository.findByNameContainingIgnoreCase("A bunch of chicken").get(0);

        // create fridge & menuPlan
        MenuPlan plan = service.createEmptyMenuPlan(user, profile, LocalDate.now().minusDays(6), LocalDate.now());
        List<String> fridge = new ArrayList<>();
        fridge.add("Chicken");
        this.service.createFridge(plan, fridge);

        // custom MenuPlanContent
        plan.addContent(new MenuPlanContent(plan, 1, 1, recipe));

        recipe = recipeRepository.findByNameContainingIgnoreCase("Recipe 1").get(0);
        plan.addContent(new MenuPlanContent(plan, 1, 2, recipe));
        plan = this.menuPlanRepository.save(plan);

        this.service.createInventory(plan.getUser());

        InventoryListDto inventory = this.service.searchInventory(plan.getId());

        this.service.updateInventoryIngredient(plan.getUser(), inventory.available().get(0).setInventoryStatus(false));
        inventory = this.service.searchInventory(plan.getId());

        assertEquals(1, inventory.available().size());
        assertEquals(2, inventory.missing().size());

        this.service.updateInventoryIngredient(plan.getUser(), inventory.missing().get(0).setInventoryStatus(true));
        inventory = this.service.searchInventory(plan.getId());

        assertEquals(2, inventory.available().size());
        assertEquals(1, inventory.missing().size());
    }

    @Test
    void updateInvalidInventoryIngredient() throws Exception {
        // Creating Inventory
        Recipe r = new Recipe();
        r.setName("A bunch of chicken");
        r.setInstructions("Recipe Instructions");
        r = recipeRepository.save(r);

        // adding chicken
        var ing = new Ingredient();
        ing.setName("Chicken");
        ingredientRepository.save(ing);

        // adding rids
        Set<RecipeIngredient> ris = new HashSet<>();
        List<RecipeIngredientDetails> rids = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            RecipeIngredientDetails rid = new RecipeIngredientDetails();
            rid.setDescriber("some");
            rid.setAmount(1.5f);
            rid.setUnit(FoodUnit.ounce);
            rid.setIngredient("Chicken TESTED");
            rids.add(recipeIngredientDetailsRepository.save(rid));

            RecipeIngredient ri = new RecipeIngredient();
            ri.setIngredient(ing);
            ri.setAmount(rid);
            ri.setRecipe(r);
            recipeIngredientRepository.save(ri);
        }

        r.setIngredients(ris);
        recipeRepository.save(r);

        Recipe recipe = recipeRepository.findByNameContainingIgnoreCase("A bunch of chicken").get(0);

        // create fridge & menuPlan
        MenuPlan plan = service.createEmptyMenuPlan(user, profile, LocalDate.now().minusDays(6), LocalDate.now());
        List<String> fridge = new ArrayList<>();
        fridge.add("Chicken");
        this.service.createFridge(plan, fridge);

        // custom MenuPlanContent
        plan.addContent(new MenuPlanContent(plan, 1, 1, recipe));

        recipe = recipeRepository.findByNameContainingIgnoreCase("Recipe 1").get(0);
        plan.addContent(new MenuPlanContent(plan, 1, 2, recipe));
        plan = this.menuPlanRepository.save(plan);

        this.service.createInventory(plan.getUser());

        InventoryListDto inventory = this.service.searchInventory(plan.getId());

        this.service.updateInventoryIngredient(plan.getUser(), inventory.available().get(0).setInventoryStatus(false));
        inventory = this.service.searchInventory(plan.getId());

        assertEquals(1, inventory.available().size());
        assertEquals(2, inventory.missing().size());

        this.service.updateInventoryIngredient(plan.getUser(), inventory.missing().get(0).setInventoryStatus(true));
        inventory = this.service.searchInventory(plan.getId());

        assertEquals(2, inventory.available().size());
        assertEquals(1, inventory.missing().size());

        InventoryIngredientDto rogueOne = inventory.missing().get(0)
            .setName("Rogue");

        MenuPlan finalPlan = plan;
        assertThrows(ValidationException.class, () -> this.service.updateInventoryIngredient(finalPlan.getUser(), rogueOne));
    }
}
