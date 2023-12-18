package at.ac.tuwien.sepr.groupphase.backend.unittests.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.AllergeneDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.IngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.AllergeneMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.IngredientMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Allergene;
import at.ac.tuwien.sepr.groupphase.backend.entity.Ingredient;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.AllergeneRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.IngredientRepository;
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

import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class ProfileServiceTest {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private AllergeneRepository allergeneRepository;

    @Autowired
    private AllergeneMapper allergeneMapper;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private IngredientMapper ingredientMapper;

    private Long allergeneId;
    private Long ingredientId;
    private Long profileId;

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
    }

    @Test
    public void createProfileWithValidAttributeReturnsCorrectData() throws ValidationException, NotFoundException {
        ProfileDto testProfileDto = ProfileDto.ProfileDtoBuilder.aProfileDto()
            .withName("Profile User test")
            .withAllergens(List.of(
                AllergeneDto.AllergeneDtoBuilder.anAllergeneDto().withId(1L).withName("Gluten").build())
            )
            .withIngredient(List.of(
                IngredientDto.IngredientDtoBuilder.anIngredientDto().withId(1L).withName("Rice").build())
            ).build();

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
            ).build();

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
            ).build();

        Assertions.assertThrows(NotFoundException.class,
            () -> profileService.saveProfile(testProfileDto));
    }

    @AfterEach
    public void deleteTestData() {
       // allergeneRepository.deleteById(allergeneId);
       // ingredientRepository.deleteById(ingredientId);
    }

}
