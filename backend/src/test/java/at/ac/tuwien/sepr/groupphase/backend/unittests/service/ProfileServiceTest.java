package at.ac.tuwien.sepr.groupphase.backend.unittests.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.AllergeneDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.IngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileUserDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeRatingDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeRatingListsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.AllergeneMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.IngredientMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ProfileMapperImpl;
import at.ac.tuwien.sepr.groupphase.backend.entity.Allergene;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.FoodUnit;
import at.ac.tuwien.sepr.groupphase.backend.entity.Ingredient;
import at.ac.tuwien.sepr.groupphase.backend.entity.Profile;
import at.ac.tuwien.sepr.groupphase.backend.entity.Recipe;
import at.ac.tuwien.sepr.groupphase.backend.entity.RecipeIngredient;
import at.ac.tuwien.sepr.groupphase.backend.entity.RecipeIngredientDetails;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.AllergeneRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.IngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ProfileRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeIngredientDetailsRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeIngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
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

import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    private Long profileId3;

    private Long testUserId;
    private Long testUserId2;

    private long recipe1Id;
    private long recipeIngredient1Id;
    private long ingredient1Id;
    private long rd1Id;

    @BeforeEach
    public void generateTestData() {

        GenerateAllergeneAndIngredientAndTestProfiles();

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

    private void GenerateAllergeneAndIngredientAndTestProfiles() {
        AllergeneDto allergeneDto = AllergeneDto.AllergeneDtoBuilder.anAllergeneDto().withId(1L).withName("Gluten").build();
        Allergene savedAllergene = allergeneRepository.save(allergeneMapper.allergeneDtoToAllergene(allergeneDto));
        allergeneId = savedAllergene.getId();

        IngredientDto ingredientDto = IngredientDto.IngredientDtoBuilder.anIngredientDto().withId(1L).withName("Rice").build();
        Ingredient savedIngredient = ingredientRepository.save(ingredientMapper.ingredientDtoToIngredient(ingredientDto));
        ingredientId = savedIngredient.getId();

        var testUser = new ApplicationUser().setId(-1L).setEmail("John@test.at")
            .setPasswordEncoded("test").setNickname("John Doe")
            .setUserPicture(Base64.getDecoder().decode("abcd"));
        testUserId = userRepository.save(testUser).getId();

        var testUser2 = new ApplicationUser().setId(-2L).setEmail("Jane@test.at")
            .setPasswordEncoded("test").setNickname("Jane Doe")
            .setUserPicture(Base64.getDecoder().decode("abcd"));
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
            .withUser(testUser)
            .build();

        ProfileUserDto profileDto3 = ProfileUserDto.ProfileDtoBuilder.aProfileDto()
            .withName("Indian")
            .withAllergens(Collections.singletonList(allergeneMapper.allergeneToAllergeneDto(savedAllergene)))
            .withIngredient(Collections.singletonList(ingredientMapper.ingredientToIngredientDto(savedIngredient)))
            .withUser(testUser2)
            .build();

        try {
            profileId = profileRepository.save(profileMapper.profileDtoToProfile(profileDto)).getId();
            profileId2 = profileRepository.save(profileMapper.profileDtoToProfile(profileDto2)).getId();
            profileId3 = profileRepository.save(profileMapper.profileDtoToProfile(profileDto3)).getId();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @AfterEach
    public void deleteTestData() {
        // Clearing user profiles and active profiles
        userRepository.findById(testUserId).ifPresent(user -> {
            user.setActiveProfile(null);
            user.getProfiles().clear();
            userRepository.save(user);
        });
        userRepository.findById(testUserId2).ifPresent(user -> {
            user.setActiveProfile(null);
            user.getProfiles().clear();
            userRepository.save(user);
        });

        // Deleting specific entities by their IDs
        recipeIngredientRepository.deleteById(recipeIngredient1Id);
        recipeIngredientDetailsRepository.deleteById(rd1Id);
        profileRepository.deleteById(profileId);
        profileRepository.deleteById(profileId2);
        profileRepository.deleteById(profileId3);
        allergeneRepository.deleteById(allergeneId);
        ingredientRepository.deleteById(ingredient1Id);
        ingredientRepository.deleteById(ingredientId);
        userRepository.deleteById(testUserId);
        userRepository.deleteById(testUserId2);
        recipeRepository.deleteById(recipe1Id);
    }


    @Test
    public void searchProfilesWithNameReturnsCorrectResults() {
        ProfileSearchDto searchDto = new ProfileSearchDto("", "Asia", true, 0, 20);
        ProfileSearchResultDto result = profileService.searchProfiles(searchDto, testUserId);
        ProfileDetailDto resultProfile = result.profiles().get(0);
        assertAll("Search result validation",
            () -> assertNotNull(result, "Result should not be null"),
            () -> assertEquals(1, result.profiles().size(), "Result should contain one profile"),
            () -> assertEquals("Asian", resultProfile.name(), "Profile name should be 'Asian'"),
            () -> assertEquals(profileId, resultProfile.id(), "Profile ID should match the expected profileId"),
            () -> assertEquals(testUserId, resultProfile.userId(), "Profile ID should match the expected profileId"),
            () -> assertEquals("Gluten", resultProfile.allergens().get(0), "First allergen should be 'Gluten'"),
            () -> assertEquals("Rice", resultProfile.ingredients().get(0), "First ingredient should be 'Rice'")
        );
    }

    @Test
    public void searchProfilesWithDifferentUserContextsReturnsCorrectResults() {
        ProfileSearchDto searchDto = new ProfileSearchDto("", "", true, 0, 20);
        // Search with first user context
        ProfileSearchResultDto resultUser1 = profileService.searchProfiles(searchDto, testUserId);
        // Search with second user context
        ProfileSearchResultDto resultUser2 = profileService.searchProfiles(searchDto, testUserId2);
        assertAll("Search results validation for different user contexts",
            () -> assertEquals(2, resultUser1.profiles().size(), "Number of profiles for User1 should match"),
            () -> assertEquals(1, resultUser2.profiles().size(), "Number of profiles for User2 should match"),
            () -> assertNotEquals(resultUser1.profiles(), resultUser2.profiles(), "Profiles for User1 and User2 should not be the same")
        );
    }

    @Test
    public void searchNotOwnProfilesWithDifferentUserContextsReturnsCorrectResults() {
        ProfileSearchDto searchDto = new ProfileSearchDto("", "", false, 0, 20);
        // Search with first user context
        ProfileSearchResultDto resultUser1 = profileService.searchProfiles(searchDto, testUserId);
        // Search with second user context
        ProfileSearchResultDto resultUser2 = profileService.searchProfiles(searchDto, testUserId2);
        assertAll("Search results validation for different user contexts",
            () -> assertEquals(1, resultUser1.profiles().size(), "Number of profiles for User1 should match"),
            () -> assertEquals(2, resultUser2.profiles().size(), "Number of profiles for User2 should match"),
            () -> assertNotEquals(resultUser1.profiles(), resultUser2.profiles(), "Profiles for User1 and User2 should not be the same")
        );
    }


    @Test
    public void searchProfilesWithNonExistentNameReturnsNoResults() {
        String nonExistentName = "NameNotInDatabase12345";
        ProfileSearchDto searchDto = new ProfileSearchDto("", nonExistentName, false, 0, 20);
        ProfileSearchResultDto result = profileService.searchProfiles(searchDto, testUserId);

        assertAll("Search result validation for non-existent name",
            () -> assertNotNull(result, "Result should not be null"),
            () -> assertTrue(result.profiles().isEmpty(), "Result should contain no profiles")
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

        assertNotNull(createdProfile);

        assertAll(
            () -> assertEquals(testProfileDto.getName(), createdProfile.getName()),
            () -> assertEquals(testProfileDto.getAllergens().toString(), createdProfile.getAllergens().toString()),
            () -> assertEquals(testProfileDto.getIngredient().toString(), createdProfile.getIngredient().toString())
        );
        userRepository.findById(testUserId).ifPresent(user -> {
            user.setActiveProfile(null);
            userRepository.save(user);
        });
        profileRepository.deleteById(profileRepository.findByName("Profile User test").getId());
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
    public void rateRecipeWithExistingProfileAndIncorrectRatingIntegerThrowValidationException() {
        RecipeRatingDto recipeRatingDto = new RecipeRatingDto(recipe1Id, testUserId, 2);

        Assertions.assertThrows(ValidationException.class,
            () -> profileService.rateRecipe(recipeRatingDto));
    }

    @Test
    public void getRatingListsOfExistingProfileReturnsRatingListsOfProfile() {
        RecipeRatingListsDto ratingLists = profileService.getRatingLists(testUserId2);

        assertAll("Rating Lists result validation",
            () -> assertNotNull(ratingLists, "Result should not be null"),
            () -> assertEquals(1, ratingLists.dislikes().size(), "The dislikes list should contain one recipe ID"),
            () -> assertEquals(0, ratingLists.likes().size(), "The likes list should contain zero recipe IDs"),
            () -> assertEquals(recipe1Id, ratingLists.dislikes().get(0))
        );
    }

}
