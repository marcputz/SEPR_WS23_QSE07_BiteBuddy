package at.ac.tuwien.sepr.groupphase.backend.unittests.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.*;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.AllergeneMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.IngredientMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ProfileMapperImpl;
import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.*;
import at.ac.tuwien.sepr.groupphase.backend.service.ProfileService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class ProfileServiceTest {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AllergeneRepository allergeneRepository;

    @Autowired
    private AllergeneMapper allergeneMapper;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private IngredientMapper ingredientMapper;

    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private ProfileMapperImpl profileMapper;
    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private RecipeIngredientRepository recipeIngredientRepository;

    @Autowired
    private RecipeIngredientDetailsRepository recipeIngredientDetailsRepository;

    private Long allergeneId;
    private Long ingredientId;
    private Long profileId;
    private Long profileId2;
    private Long testUserId;
    private Long testUserId2;

    private long recipe1Id;
    private long recipeIngredient1Id;
    private long ingredient1Id;
    private long rd1Id;

    @BeforeEach
    public void generateTestData() {

        AllergeneDto allergeneDto = AllergeneDto.AllergeneDtoBuilder
            .anAllergeneDto().withId(1L).withName("Gluten")
            .build();
        Allergene savedAllergene = allergeneRepository.save(allergeneMapper.allergeneDtoToAllergene(allergeneDto));
        allergeneId = savedAllergene.getId();
        IngredientDto ingredientDto = IngredientDto.IngredientDtoBuilder
            .anIngredientDto().withId(1L).withName("Rice")
            .build();

        Ingredient savedIngredient = ingredientRepository.save(ingredientMapper.ingredientDtoToIngredient(ingredientDto));
        ingredientId = savedIngredient.getId();

        String base64EncodedImage = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAIAAACQd1PeAAAADElEQVR4nGP4z8AAAAMBAQDJ/pLvAAAAAElFTkSuQmCC"; //1x1 Red PNG
        byte[] imageBytes = Base64.getDecoder().decode(base64EncodedImage);

        String email1 = "John@test.at";
        String nickname1 = "John Doe";

        var testUser = new ApplicationUser().setId(-1L).setEmail(email1)
            .setPasswordEncoded("ba527ca265c37cf364b057b4f412d175f79d363e0e15d709097f188a4fe979ba2cc1c048e1c97da7804465cef5f8abe7") // "password"
            .setNickname(nickname1)
            .setUserPicture(imageBytes);
        testUserId = userRepository.save(testUser).getId();

        String email2 = "Jane@test.at";
        String nickname2 = "Jane Doe";

        var testUser2 = new ApplicationUser().setId(-2L).setEmail(email2)
            .setPasswordEncoded("ba527ca265c37cf364b057b4f412d175f79d363e0e15d709097f188a4fe979ba2cc1c048e1c97da7804465cef5f8abe7") // "password"
            .setNickname(nickname2)
            .setUserPicture(imageBytes);
        testUserId2 = userRepository.save(testUser2).getId();

        ProfileUserDto profileDto = ProfileUserDto.ProfileDtoBuilder.aProfileDto()
            .withName("Asian")
            .withAllergens(Collections.singletonList(allergeneMapper.allergeneToAllergeneDto(savedAllergene)))
            .withIngredient(Collections.singletonList(ingredientMapper.ingredientToIngredientDto(savedIngredient)))
            .withUser(testUser)
            .build();

        ProfileUserDto profileDto2 = ProfileUserDto.ProfileDtoBuilder.aProfileDto()
            .withName("Italian")
            .withAllergens(Collections.singletonList(allergeneMapper.allergeneToAllergeneDto(savedAllergene)))
            .withIngredient(Collections.singletonList(ingredientMapper.ingredientToIngredientDto(savedIngredient)))
            .withUser(testUser2)
            .build();

        try {
            profileId = profileRepository.save(profileMapper.profileDtoToProfile(profileDto)).getId();
            profileId2 = profileRepository.save(profileMapper.profileDtoToProfile(profileDto2)).getId();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // creating ingredients
        // adding apple
        Ingredient ingredient1 = new Ingredient();
        ingredient1.setName("Apple");
        ingredient1Id = ingredientRepository.save(ingredient1).getId();

        // creating recipes without ingredients
        Recipe recipe1 = new Recipe();
        recipe1.setInstructions("Instructions 1");
        recipe1.setName("recipe 1");
        recipe1Id = recipeRepository.save(recipe1).getId();

        // creating recipeIngredientDetails
        RecipeIngredientDetails r1 = new RecipeIngredientDetails();
        r1.setDescriber("little");
        r1.setUnit(FoodUnit.tablespoon);
        r1.setIngredient("sugar");
        r1.setAmount(1);
        rd1Id = recipeIngredientDetailsRepository.save(r1).getId();

        // creating recipeIngredients
        RecipeIngredient recipeIngredient1 = new RecipeIngredient();
        recipeIngredient1.setIngredient(ingredient1);
        recipeIngredient1.setAmount(r1);
        recipeIngredient1.setRecipe(recipe1);
        recipeIngredient1Id = recipeIngredientRepository.save(recipeIngredient1).getId();

        // updating recipes with ingredients
        Set<RecipeIngredient> ringredients1 = new HashSet<>();
        ringredients1.add(recipeIngredient1);
        recipeRepository.updateIngredients(recipe1Id, ringredients1);

        Profile profile2 = profileRepository.getReferenceById(profileId2);
        profile2.getDisliked().add(recipe1);
        profileRepository.save(profile2);

        userRepository.save(userRepository.getReferenceById(testUserId2).setActiveProfile(profileRepository.getReferenceById(profileId2)));

    }


    @AfterEach
    public void deleteTestUser() {
        List<ApplicationUser> users = userRepository.findAll();
        for (ApplicationUser user : users) {
            user.setActiveProfile(null);
            user.getProfiles().clear();
            userRepository.save(user);
        }

        recipeIngredientRepository.deleteById(recipeIngredient1Id);
        recipeIngredientDetailsRepository.deleteById(rd1Id);
        ingredientRepository.deleteById(ingredient1Id);

        profileRepository.deleteAll();
        allergeneRepository.deleteAll();
        ingredientRepository.deleteAll();
        userRepository.deleteAll();

        recipeRepository.deleteById(recipe1Id);


    }

    @Test
    public void searchProfilesWithNameReturnsCorrectResults() {

        // Define search parameters
        ProfileSearchDto searchDto = new ProfileSearchDto("", "Asia", testUserId, 0, 20);

        ProfileSearchResultDto result = profileService.searchProfiles(searchDto);

        ProfileListDto resultProfile = result.profiles().get(0);

        Assertions.assertAll("Search result validation",
            () -> Assertions.assertNotNull(result, "Result should not be null"),
            () -> Assertions.assertEquals(1, result.profiles().size(), "Result should contain one profile"),
            () -> Assertions.assertEquals("Asian", resultProfile.name(), "Profile name should be 'Asian'"),
            () -> Assertions.assertEquals(profileId, resultProfile.userId(), "User ID should match the expected profileId"),
            () -> Assertions.assertEquals("Gluten", resultProfile.allergens().get(0).getName(), "First allergen should be 'Gluten'"),
            () -> Assertions.assertEquals("Rice", resultProfile.ingredients().get(0).getName(), "First ingredient should be 'Rice'")
        );
    }

    @Test
    public void createProfileWithValidAttributeReturnsCorrectData() throws ValidationException, NotFoundException {
        ProfileDto testProfileDto = ProfileDto.ProfileDtoBuilder.aProfileDto()
            .withName("Profile User test")
            .withAllergens(List.of(
                AllergeneDto.AllergeneDtoBuilder.anAllergeneDto().withId(allergeneId).withName("Gluten").build())
            )
            .withIngredient(List.of(
                IngredientDto.IngredientDtoBuilder.anIngredientDto().withId(ingredientId).withName("Rice").build())
            ).withUserId(testUserId).build();

        ProfileDto createdProfile = profileService.saveProfile(testProfileDto);
        //profileId = createdProfile.getId();

        Assertions.assertNotNull(createdProfile);

        Assertions.assertAll(
            //   () -> Assertions.assertNotNull(createdProfile.getId()),
            () -> Assertions.assertEquals(testProfileDto.getName(), createdProfile.getName()),
            () -> Assertions.assertEquals(testProfileDto.getAllergens().toString(), createdProfile.getAllergens().toString()),
            () -> Assertions.assertEquals(testProfileDto.getIngredient().toString(), createdProfile.getIngredient().toString())
        );
    }

    @Test
    public void createProfileWithNonExistingAllergensThrowNotFoundException() {
        ProfileDto testProfileDto = ProfileDto.ProfileDtoBuilder.aProfileDto()
            .withName("Profile User test")
            .withAllergens(List.of(
                AllergeneDto.AllergeneDtoBuilder.anAllergeneDto().withId(8L).withName("Gluten").build())
            )
            .withIngredient(List.of(
                IngredientDto.IngredientDtoBuilder.anIngredientDto().withId(1L).withName("Rice").build())
            ).withUserId(testUserId).build();

        Assertions.assertThrows(NotFoundException.class,
            () -> profileService.saveProfile(testProfileDto));
    }

    @Test
    public void createProfileWithNonExistingIngredientThrowNotFoundException() {
        ProfileDto testProfileDto = ProfileDto.ProfileDtoBuilder.aProfileDto()
            .withName("Profile User test")
            .withAllergens(List.of(
                AllergeneDto.AllergeneDtoBuilder.anAllergeneDto().withId(1L).withName("Gluten").build())
            )
            .withIngredient(List.of(
                IngredientDto.IngredientDtoBuilder.anIngredientDto().withId(4L).withName("Rice").build())
            ).withUserId(testUserId).build();

        Assertions.assertThrows(NotFoundException.class,
            () -> profileService.saveProfile(testProfileDto));
    }

    @Test
    public void rateRecipeWithExistingProfileAndIncorrectRatingIntegerThrowValidationException(){
        RecipeRatingDto recipeRatingDto = new RecipeRatingDto(recipe1Id, testUserId, 2);

        Assertions.assertThrows(ValidationException.class,
            () -> profileService.rateRecipe(recipeRatingDto));
    }

    @Test
    public void getRatingListsOfExistingProfileReturnsRatingListsOfProfile(){
        RecipeRatingListsDto ratingLists = profileService.getRatingLists(testUserId2);

        Assertions.assertAll("Rating Lists result validation",
            () -> Assertions.assertNotNull(ratingLists, "Result should not be null"),
            () -> Assertions.assertEquals(1, ratingLists.dislikes().size(), "The dislikes list should contain one recipe ID"),
            () -> Assertions.assertEquals(0, ratingLists.likes().size(), "The likes list should contain zero recipe IDs"),
            () -> Assertions.assertEquals(profileId, ratingLists.dislikes().get(0), recipe1Id)
        );
    }

}
