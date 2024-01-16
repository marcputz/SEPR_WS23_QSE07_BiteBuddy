package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.datainsert.JsonFileReader;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.repository.AllergeneIngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.AllergeneRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.IngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.MenuPlanRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PasswordResetRequestRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ProfileRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeIngredientDetailsRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeIngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeRatingRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.lang.invoke.MethodHandles;

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
                         RecipeIngredientDetailsRepository recipeIngredientDetailsRepository) {
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
        userRepository.deleteAll();
    }

    private void insertRecipeData() {
        JsonFileReader jsonDataInsert = new JsonFileReader(recipeRepository, ingredientRepository,
            allergeneRepository, allergeneIngredientRepository,
            recipeIngredientRepository, recipeIngredientDetailsRepository);
        jsonDataInsert.putFoodDataInDataBase();
    }

    private void generateUserData() {
        LOGGER.debug("Generating user data");

        ApplicationUser user1 = new ApplicationUser().setId(-1L).setEmail("max.mustermann@test.at")
            .setPasswordEncoded(PasswordEncoder.encode("5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", "max.mustermann@test.at")) // "password"
            .setNickname("maxmuster");
        userRepository.save(user1);

        ApplicationUser user2 = new ApplicationUser().setId(-2L).setEmail("mail@marcputz.at")
            .setPasswordEncoded(PasswordEncoder.encode("5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", "mail@marcputz.at")) // "password"
            .setNickname("marcputz");
        userRepository.save(user2);
    }
}
