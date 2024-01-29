package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.*;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.authentication.LoginDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.AllergeneIngredientMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.AllergeneMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.IngredientMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ProfileMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.exception.AuthenticationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.*;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.lang.invoke.MethodHandles;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ProfileEndpointTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AllergeneRepository allergeneRepository;
    @Autowired
    private IngredientRepository ingredientRepository;
    @Autowired
    private RecipeRepository recipeRepository;
    @Autowired
    private AllergeneMapper allergeneMapper;
    @Autowired
    private IngredientMapper ingredientMapper;
    @Autowired
    private ProfileMapper profileMapper;
    @Autowired
    private AllergeneIngredientMapper allergeneIngredientMapper;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private AllergeneIngredientRepository allergeneIngredientRepository;
    @Autowired
    private AuthenticationService authenticationService;
    private Long testUserId;
    private Long recipeId;
    private Long allergeneId;
    private Long ingredientId;
    private Long allergeneIngredientId;
    private String testUserAuthToken;
    private Long profileId;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void generateTestData() throws Exception {
        AllergeneDto allergeneDto = AllergeneDto.AllergeneDtoBuilder.anAllergeneDto().withId(1L).withName("Gluten").build();
        Allergene savedAllergene = allergeneRepository.save(allergeneMapper.allergeneDtoToAllergene(allergeneDto));
        allergeneId = savedAllergene.getId();

        IngredientDto ingredientDto = IngredientDto.IngredientDtoBuilder.anIngredientDto().withId(1L).withName("Rice").build();
        Ingredient savedIngredient = ingredientRepository.save(ingredientMapper.ingredientDtoToIngredient(ingredientDto));
        ingredientId = savedIngredient.getId();


        // creating recipes without ingredients
        Recipe recipe1 = new Recipe();
        recipe1.setInstructions("Instructions 1");
        recipe1.setName("recipe 1");
        recipe1.setId(1L);
        recipeId = recipeRepository.save(recipe1).getId();

        String testUserPassword = "test";
        var testUser = new ApplicationUser().setId(-1L).setEmail("John@test.at")
            .setPasswordEncoded(PasswordEncoder.encode(testUserPassword, "John@test.at")).setNickname("John Doe")
            .setUserPicture(Base64.getDecoder().decode("abcd"));
        testUserId = userRepository.save(testUser).getId();

        ProfileUserDto profileDto = ProfileUserDto.ProfileDtoBuilder.aProfileDto()
            .withName("Asian")
            .withAllergens(Collections.singletonList(allergeneMapper.allergeneToAllergeneDto(savedAllergene)))
            .withIngredient(Collections.singletonList(ingredientMapper.ingredientToIngredientDto(savedIngredient)))
            .withUser(testUser)
            .build();

        try {
            profileId = profileRepository.save(profileMapper.profileDtoToProfile(profileDto)).getId();
        } catch (Exception e) {
            throw new RuntimeException("Exception while Generating Test Data, SaveProfile", e);
        }

        ApplicationUser user = userRepository.getReferenceById(testUserId);
        user.setActiveProfile(profileRepository.getReferenceById(profileId));
        userRepository.save(user);

        //Login
        LoginDto loginDto = new LoginDto()
            .setEmail(testUser.getEmail())
            .setPassword(testUserPassword);
        try {
            testUserAuthToken = authenticationService.loginUser(loginDto);
        } catch (AuthenticationException e) {
            throw new RuntimeException("Exception while logging in test user", e);
        }
    }

    @AfterEach
    public void deleteTestData() {
        // Logout
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.set("authorization", testUserAuthToken);
        try {
            this.mockMvc.perform(post("/api/v1/authentication/logout")
                    .headers(requestHeaders))
                .andReturn();
        } catch (Exception e) {
            LOGGER.error("Error at deleteTestData logout", e);
        }

        // Clear data in users
        userRepository.findAll().forEach(user -> {
            user.setActiveProfile(null);
            //user.getProfiles().clear();
            userRepository.save(user);
        });

        profileRepository.deleteAll();
        userRepository.deleteAll();
        allergeneIngredientRepository.deleteAll();
        allergeneRepository.deleteAll();
        ingredientRepository.deleteAll();
    }

    @Test
    public void searchProfilesWithNameReturnsCorrectProfile() throws Exception {
        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        updateHeaders.setContentType(MediaType.APPLICATION_JSON);
        updateHeaders.set("Authorization", testUserAuthToken);

        // Perform update request
        var searchResult = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/profiles/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                    "creator": "",
                    "name": "Asia",
                    "ownProfiles": "true",
                    "page": 0,
                    "entriesPerPage": 20
                    }
                    """)
                .headers(updateHeaders)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
        ProfileSearchResultDto profileSearchResultDto = objectMapper.readerFor(ProfileSearchResultDto.class).readValue(searchResult);
        ProfileDetailDto resultProfile = profileSearchResultDto.profiles().get(0);
        assertAll("Search result validation",
            () -> assertNotNull(profileSearchResultDto, "Result should not be null"),
            () -> assertEquals(1, profileSearchResultDto.profiles().size(), "Result should contain one profile"),
            () -> assertEquals("Asian", resultProfile.name(), "Profile name should be 'Asian'"),
            () -> assertEquals(profileId, resultProfile.id(), "Profile ID should match the expected profileId"),
            () -> assertEquals(testUserId, resultProfile.userId(), "Profile ID should match the expected profileId"),
            () -> assertEquals("Gluten", resultProfile.allergens().get(0).getName(), "First allergen should be 'Gluten'"),
            () -> assertEquals("Rice", resultProfile.ingredients().get(0).getName(), "First ingredient should be 'Rice'")
        );
    }

    @Test
    public void copyToOwn_ShouldCopyProfileSuccessfully() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", testUserAuthToken);

        Long profileToCopyId = profileId;

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/profiles/copyToOwn/" + profileToCopyId)
                .headers(headers)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        ProfileDetailDto resultProfileDto = objectMapper.readValue(response.getContentAsString(), ProfileDetailDto.class);

        assertNotNull(resultProfileDto, "Copied profile should not be null");
        assertEquals("Asian", resultProfileDto.name(), "Profile name should be 'Asian'");
        assertEquals(testUserId, resultProfileDto.userId(), "User Id should be same as testUserId");
        profileRepository.deleteById(resultProfileDto.id());
    }

    @Test
    public void createProfileWithValidProfileDtoShouldCreateProfile() throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", testUserAuthToken);

        Allergene allergene1 = allergeneRepository.getReferenceById(allergeneId);
        AllergeneDto allergeneDto = allergeneMapper.allergeneToAllergeneDto(allergene1);

        ArrayList<AllergeneDto> allergenes = new ArrayList<>();
        allergenes.add(allergeneDto);

        Ingredient ingredient1 = ingredientRepository.getReferenceById(ingredientId);
        IngredientDto ingredientDto = ingredientMapper.ingredientToIngredientDto(ingredient1);
        ArrayList<IngredientDto> ingredients = new ArrayList<>();
        ingredients.add(ingredientDto);

        ProfileDto profileToCreate = new ProfileDto.ProfileDtoBuilder().withName("Hangry").withAllergens(allergenes).withIngredient(ingredients).withUserId(testUserId).build();

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/profiles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(profileToCreate))
                .headers(headers)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        ProfileDto resultProfileDto = objectMapper.readValue(response.getContentAsString(), ProfileDto.class);

        assertAll(
            () -> assertNotNull(resultProfileDto, "created profile should not be null"),
            () -> assertEquals(testUserId, resultProfileDto.getUserId(), "User Id should be same as testUserId"),
            () -> assertEquals(allergene1.getId(), resultProfileDto.getAllergens().get(0).getId()),
            () -> assertEquals(ingredient1.getId(), resultProfileDto.getIngredient().get(0).getId()),
            () -> assertEquals("Hangry", resultProfileDto.getName())
        );
    }

    @Test
    public void createProfileWithInvalidProfileDtoRespondsValidation() throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", testUserAuthToken);

        Allergene allergene1 = allergeneRepository.getReferenceById(allergeneId);
        AllergeneDto allergeneDto = allergeneMapper.allergeneToAllergeneDto(allergene1);

        ArrayList<AllergeneDto> allergenes = new ArrayList<>();
        allergenes.add(allergeneDto);

        Ingredient ingredient1 = ingredientRepository.getReferenceById(ingredientId);
        IngredientDto ingredientDto = ingredientMapper.ingredientToIngredientDto(ingredient1);
        ArrayList<IngredientDto> ingredients = new ArrayList<>();
        ingredients.add(ingredientDto);

        ProfileDto profileToCreate = new ProfileDto.ProfileDtoBuilder().withName("    ").withAllergens(allergenes).withIngredient(ingredients).withUserId(testUserId).build();

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/profiles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(profileToCreate))
                .headers(headers)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andReturn();
    }

    @Test
    public void setActiveProfile_ShouldSetActiveProfileSuccessfully() throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", testUserAuthToken);

        Long profileToSetActiveId = profileId;

        this.mockMvc.perform(post("/api/v1/profiles/setActive/" + profileToSetActiveId)
                .headers(headers))
            .andExpect(status().isOk());

        ApplicationUser updatedUser = userRepository.findById(testUserId).orElseThrow();
        assertNotNull(updatedUser.getActiveProfile(), "Active profile should not be null");
        assertEquals(profileToSetActiveId, updatedUser.getActiveProfile().getId(), "Active profile ID should match the set profile ID");
    }

    @Test
    public void rateRecipeWithExistingProfileAndValidRating() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", testUserAuthToken);

        RecipeRatingDto ratingDto = new RecipeRatingDto(recipeId, testUserId, 1);

        this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/profiles/rating/" + ratingDto.recipeId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(ratingDto))
                .headers(headers)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsByteArray();

        Profile activeProfile = this.profileRepository.getReferenceById(userRepository.getReferenceById(testUserId).getActiveProfile().getId());

        assertAll(
            () -> assertNotNull(activeProfile.getLiked()),
            () -> assertEquals(1, activeProfile.getLiked().size()),
            () -> assertTrue(activeProfile.getLiked().contains(recipeRepository.getReferenceById(recipeId)))
        );

    }

    @Test
    public void rateRecipeWithExistingProfileAndInvalidRatingRespondsUnprocessableEntity() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", testUserAuthToken);

        RecipeRatingDto ratingDto = new RecipeRatingDto(recipeId, testUserId, 2);

        this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/profiles/rating/" + ratingDto.recipeId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(ratingDto))
                .headers(headers)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andReturn().getResponse().getContentAsByteArray();

    }

    @Test
    public void rateRecipeWithInvalidUserIdRespondsNotFound() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", testUserAuthToken);

        RecipeRatingDto ratingDto = new RecipeRatingDto(recipeId, -100L, 1);

        this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/profiles/rating/" + ratingDto.recipeId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(ratingDto))
                .headers(headers)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andReturn().getResponse().getContentAsByteArray();

    }

    @Test
    public void rateRecipeWithInvalidRecipeIdRespondsNotFound() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", testUserAuthToken);

        RecipeRatingDto ratingDto = new RecipeRatingDto(-100L, testUserId, 1);

        this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/profiles/rating/" + ratingDto.recipeId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(ratingDto))
                .headers(headers)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andReturn().getResponse().getContentAsByteArray();

    }

    @Test
    public void editProfileWithInvalidProfileDtoIdRespondsNotFound() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", testUserAuthToken);

        Allergene allergene1 = allergeneRepository.getReferenceById(allergeneId);
        AllergeneDto allergeneDto = allergeneMapper.allergeneToAllergeneDto(allergene1);

        ArrayList<AllergeneDto> allergenes = new ArrayList<>();
        allergenes.add(allergeneDto);

        Ingredient ingredient1 = ingredientRepository.getReferenceById(ingredientId);
        IngredientDto ingredientDto = ingredientMapper.ingredientToIngredientDto(ingredient1);
        ArrayList<IngredientDto> ingredients = new ArrayList<>();
        ingredients.add(ingredientDto);

        Profile oldProfile = profileRepository.getReferenceById(profileId);

        ProfileDto profileToCreate = new ProfileDto.ProfileDtoBuilder().withName("Love Food a lot").withAllergens(allergenes).withIngredient(ingredients).withUserId(testUserId).build();
        profileToCreate.setId(-100L);

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/profiles/editProfile/" + -100L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(profileToCreate))
                .headers(headers)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andReturn();

    }


    @Test
    public void getProfileDetailsOfExistingProfile() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", testUserAuthToken);

        Profile profile = profileRepository.getReferenceById(profileId);

        MvcResult mvcResult = this.mockMvc.perform(get("/api/v1/profiles/" + profileId)
                .headers(headers)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        ProfileDetailDto resultProfileDetailDto = objectMapper.readValue(response.getContentAsString(), ProfileDetailDto.class);

        AllergeneDto allergene = allergeneMapper.allergeneToAllergeneDto(allergeneRepository.getReferenceById(allergeneId));
        allergene.setAllergeneIngredients(null);

        assertAll(
            () -> assertNotNull(resultProfileDetailDto, "returned profile should not be null"),
            () -> assertEquals(profileId, resultProfileDetailDto.id()),
            () -> assertTrue(resultProfileDetailDto.liked().isEmpty()),
            () -> assertTrue(resultProfileDetailDto.disliked().isEmpty()),
            () -> assertEquals(resultProfileDetailDto.name(), "Asian"),
            () -> assertEquals(resultProfileDetailDto.userId(), testUserId),
            () -> assertEquals(resultProfileDetailDto.allergens().get(0).getId(), allergeneId),
            () -> assertEquals(resultProfileDetailDto.ingredients().get(0).getId(), ingredientId)
        );
    }

    @Test
    public void getProfileDetailsOfNonExistingProfile() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", testUserAuthToken);

        Profile profile = profileRepository.getReferenceById(profileId);

        MvcResult mvcResult = this.mockMvc.perform(get("/api/v1/profiles/" + -100L)
                .headers(headers)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andReturn();

    }

    @Test
    public void deleteExistingProfileRespondsConflictIfItIsActiveProfile() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", testUserAuthToken);


        MvcResult mvcResult = this.mockMvc.perform(delete("/api/v1/profiles/deleteProfile/" + profileId)
                .headers(headers)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict())
            .andReturn();

    }

    @Test
    public void deleteNonExistingProfileRespondsNotFound() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", testUserAuthToken);


        MvcResult mvcResult = this.mockMvc.perform(delete("/api/v1/profiles/deleteProfile/" + -100)
                .headers(headers)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void deleteExistingProfileReturnsDeletedProfile() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", testUserAuthToken);

        Set<Ingredient> ingredients = new HashSet<>();
        Set<Allergene> allergens = new HashSet<>();

        ingredients.add(ingredientRepository.getReferenceById(ingredientId));
        allergens.add(allergeneRepository.getReferenceById(allergeneId));

        Profile profileToAdd = new Profile(-10L, "Italian", allergens, ingredients, userRepository.getReferenceById(testUserId));
        profileRepository.save(profileToAdd);

        userRepository.save(userRepository.getReferenceById(testUserId).setActiveProfile(profileToAdd));

        MvcResult mvcResult = this.mockMvc.perform(delete("/api/v1/profiles/deleteProfile/" + profileId)
                .headers(headers)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        ProfileDto resultProfileDto = objectMapper.readValue(response.getContentAsString(), ProfileDto.class);

        AllergeneDto allergene = allergeneMapper.allergeneToAllergeneDto(allergeneRepository.getReferenceById(allergeneId));
        allergene.setAllergeneIngredients(null);

        assertAll(
            () -> assertNotNull(resultProfileDto, "deleted profile should not be null"),
            () -> assertEquals(testUserId, resultProfileDto.getUserId(), "User Id should be same as testUserId"),
            () -> assertEquals(allergeneId, resultProfileDto.getAllergens().get(0).getId()),
            () -> assertEquals(ingredientId, resultProfileDto.getIngredient().get(0).getId()),
            () -> assertEquals("Asian", resultProfileDto.getName())
        );

    }

    @Test
    public void getRatingListWithValidUserIdReturnsRatingLists() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", testUserAuthToken);

        Set<Recipe> liked = new HashSet<>();
        liked.add(recipeRepository.getReferenceById(recipeId));
        Profile profile = profileRepository.getReferenceById(profileId);
        profile.setLiked(liked);
        profileRepository.save(profile);


        MvcResult mvcResult = this.mockMvc.perform(get("/api/v1/profiles/rating/" + testUserId)
                .headers(headers)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        RecipeRatingListsDto resultRecipeRatingListsDto = objectMapper.readValue(response.getContentAsString(), RecipeRatingListsDto.class);


        assertAll(
            () -> assertNotNull(resultRecipeRatingListsDto, "rated recipe lists dto should not be null"),
            () -> assertTrue(resultRecipeRatingListsDto.likes().contains(recipeId), "User Id should be same as testUserId"),
            () -> assertTrue(resultRecipeRatingListsDto.dislikes().isEmpty())
        );
    }

    @Test
    public void getRatingListWithInValidUserIdRespondsNotFound() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", testUserAuthToken);

        Set<Recipe> liked = new HashSet<>();
        liked.add(recipeRepository.getReferenceById(recipeId));
        Profile profile = profileRepository.getReferenceById(profileId);
        profile.setLiked(liked);
        profileRepository.save(profile);


        MvcResult mvcResult = this.mockMvc.perform(get("/api/v1/profiles/rating/" + -100L)
                .headers(headers)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andReturn();

    }
}