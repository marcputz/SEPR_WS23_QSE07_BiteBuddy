package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.AllergeneDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.IngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.LoginDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileUserDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.AllergeneMapperImpl;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.IngredientMapperImpl;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ProfileMapperImpl;
import at.ac.tuwien.sepr.groupphase.backend.entity.Allergene;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Ingredient;
import at.ac.tuwien.sepr.groupphase.backend.repository.AllergeneRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.IngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ProfileRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
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
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    private AllergeneMapperImpl allergeneMapper;
    @Autowired
    private IngredientMapperImpl ingredientMapper;
    @Autowired
    private ProfileMapperImpl profileMapper;
    @Autowired
    private ProfileRepository profileRepository;
    private Long testUserId;
    private String testUserAuthToken;
    private Long profileId;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void generateTestData() throws Exception {
        AllergeneDto allergeneDto = AllergeneDto.AllergeneDtoBuilder.anAllergeneDto().withId(1L).withName("Gluten").build();
        Allergene savedAllergene = allergeneRepository.save(allergeneMapper.allergeneDtoToAllergene(allergeneDto));

        IngredientDto ingredientDto = IngredientDto.IngredientDtoBuilder.anIngredientDto().withId(1L).withName("Rice").build();
        Ingredient savedIngredient = ingredientRepository.save(ingredientMapper.ingredientDtoToIngredient(ingredientDto));
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
        //Login
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/authentication/login")
                .content((new ObjectMapper()).writeValueAsString(LoginDto.LoginDtobuilder.anLoginDto()
                    .withEmail(testUser.getEmail())
                    .withPassword(testUserPassword)
                    .build()))
                .headers(requestHeaders)).andExpect(status().isOk())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        testUserAuthToken = response.getContentAsString();
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


}
