package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.datainsert.JsonFileReader;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan.MenuPlanDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Allergene;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.MenuPlan;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.AllergeneIngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.AllergeneRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.IngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.MenuPlanRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PasswordResetRequestRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PictureRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ProfileRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeIngredientDetailsRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeIngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeRatingRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.MenuPlanService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Profile("generateData")
@Component
public class DataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserRepository userRepository;
    private final MenuPlanRepository menuPlanRepository;
    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final AllergeneRepository allergeneRepository;
    private final AllergeneIngredientRepository allergeneIngredientRepository;
    private final ProfileRepository profileRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final RecipeRatingRepository recipeRatingRepository;
    private final PasswordResetRequestRepository passwordResetRepository;
    private final RecipeIngredientDetailsRepository recipeIngredientDetailsRepository;
    private final PictureRepository pictureRepository;
    private final MenuPlanService menuPlanService;

    public DataGenerator(UserRepository userRepository,
                         MenuPlanRepository menuPlanRepository,
                         RecipeRepository recipeRepository,
                         IngredientRepository ingredientRepository,
                         AllergeneRepository allergeneRepository,
                         AllergeneIngredientRepository allergeneIngredientRepository,
                         ProfileRepository profileRepository,
                         RecipeIngredientRepository recipeIngredientRepository,
                         RecipeRatingRepository recipeRatingRepository,
                         PasswordResetRequestRepository passwordResetRepository,
                         RecipeIngredientDetailsRepository recipeIngredientDetailsRepository,
                         PictureRepository pictureRepository,
                         MenuPlanService menuPlanService) {
        this.userRepository = userRepository;
        this.menuPlanRepository = menuPlanRepository;
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.allergeneRepository = allergeneRepository;
        this.allergeneIngredientRepository = allergeneIngredientRepository;
        this.profileRepository = profileRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.recipeRatingRepository = recipeRatingRepository;
        this.passwordResetRepository = passwordResetRepository;
        this.recipeIngredientDetailsRepository = recipeIngredientDetailsRepository;
        this.pictureRepository = pictureRepository;
        this.menuPlanService = menuPlanService;
    }

    @PostConstruct
    public void generateData() {
        eraseData();

        insertRecipeData();

        generateUserData();
    }

    private void eraseData() {
        LOGGER.debug("Erasing all data from DB");

        // erase relationship tables
        allergeneIngredientRepository.deleteAll();
        recipeIngredientRepository.deleteAll();

        // delete detail data tables
        recipeRatingRepository.deleteAll();

        // delete main tables
        menuPlanRepository.deleteAll();
        recipeRepository.deleteAll();
        ingredientRepository.deleteAll();
        allergeneRepository.deleteAll();
        profileRepository.deleteAll();
        passwordResetRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void insertRecipeData() {
        JsonFileReader jsonDataInsert = new JsonFileReader(recipeRepository, ingredientRepository,
            allergeneRepository, allergeneIngredientRepository,
            recipeIngredientRepository, recipeIngredientDetailsRepository, pictureRepository);
        jsonDataInsert.putFoodDataInDataBase();
    }

    private void generateUserData() {
        LOGGER.debug("Generating user data");

        ApplicationUser user1 = new ApplicationUser().setId(-1L).setEmail("max.mustermann@test.at")
            .setPasswordEncoded(
                PasswordEncoder.encode("5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", "max.mustermann@test.at")) // "password"
            .setNickname("maxmuster");
        userRepository.save(user1);

        Set<Allergene> allergens1 = new HashSet<>();
        allergens1.add(allergeneRepository.getReferenceById(1L));
        allergens1.add(allergeneRepository.getReferenceById(2L));
        allergens1.add(allergeneRepository.getReferenceById(3L));

        at.ac.tuwien.sepr.groupphase.backend.entity.Profile profile1 = new at.ac.tuwien.sepr.groupphase.backend.entity.Profile();
        profile1.setUser(user1);
        profile1.setName("Musterprofil 1");
        profile1.setAllergens(allergens1);
        var profile = profileRepository.save(profile1);
        user1.setActiveProfile(profile);
        userRepository.save(user1);

        at.ac.tuwien.sepr.groupphase.backend.entity.Profile profile2 = new at.ac.tuwien.sepr.groupphase.backend.entity.Profile();
        profile2.setUser(user1);
        profile2.setName("Musterprofil 2");
        profileRepository.save(profile2);

        ApplicationUser user2 = new ApplicationUser().setId(-2L).setEmail("mail@marcputz.at")
            .setPasswordEncoded(PasswordEncoder.encode("5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", "mail@marcputz.at")) // "password"
            .setNickname("marcputz");
        userRepository.save(user2);

        at.ac.tuwien.sepr.groupphase.backend.entity.Profile profile3 = new at.ac.tuwien.sepr.groupphase.backend.entity.Profile();
        profile3.setUser(user2);
        profile3.setName("Testprofil 1");
        profile3.setAllergens(allergens1);
        profile = profileRepository.save(profile3);
        user2.setActiveProfile(profile);
        userRepository.save(user2);

        MenuPlan menuPlan = new MenuPlan();
        LocalDate startDate = LocalDate.of(2022, 1, 1);
        LocalDate endDate = LocalDate.of(2022, 1, 7);
        try {
            menuPlan = menuPlanService.createEmptyMenuPlan(user1, profile1, startDate, endDate);
        } catch (ConflictException e) {
            LOGGER.info("conflictException during generation");
        } catch (ValidationException e) {
            LOGGER.info("validationException during generation");
        }
        try {
            menuPlanService.generateContent(menuPlan);
        } catch (ConflictException e) {
            LOGGER.info("conflictException during generation");
        }

        MenuPlan menuPlan2 = new MenuPlan();
        LocalDate startDate2 = LocalDate.of(2023, 1, 1);
        LocalDate endDate2 = LocalDate.of(2023, 1, 7);
        try {
            menuPlan2 = menuPlanService.createEmptyMenuPlan(user1, profile1, startDate2, endDate2);
        } catch (ConflictException e) {
            LOGGER.info("conflictException during generation");
        } catch (ValidationException e) {
            LOGGER.info("validationException during generation");
        }
        try {
            MenuPlanDetailDto dto = menuPlanService.generateContent(menuPlan2);
            System.out.println(dto);
        } catch (ConflictException e) {
            LOGGER.info("conflictException during generation");
        }
    }
}
