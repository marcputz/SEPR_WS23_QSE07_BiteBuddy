package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.AllergeneDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.IngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.AllergeneMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.IngredientMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ProfileMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Allergene;
import at.ac.tuwien.sepr.groupphase.backend.entity.Ingredient;
import at.ac.tuwien.sepr.groupphase.backend.entity.Profile;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.AllergeneRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.IngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ProfileRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.ProfileService;
import at.ac.tuwien.sepr.groupphase.backend.service.validation.ProfileValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
public class ProfileServiceImpl implements ProfileService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final ProfileValidator profileValidator;

    private final AllergeneRepository allergeneRepository;
    private final AllergeneMapper allergeneMapper;

    private final IngredientRepository ingredientRepository;
    private final IngredientMapper ingredientMapper;

    public ProfileServiceImpl(ProfileRepository profileRepository, AllergeneRepository allergeneRepository,
                              ProfileMapper profileMapper, AllergeneMapper allergeneMapper,
                              IngredientRepository ingredientRepository, IngredientMapper ingredientMapper,
                              ProfileValidator profileValidator) {
        this.profileRepository = profileRepository;
        this.allergeneRepository = allergeneRepository;
        this.allergeneMapper = allergeneMapper;
        this.ingredientRepository = ingredientRepository;
        this.ingredientMapper = ingredientMapper;
        this.profileMapper = profileMapper;
        this.profileValidator = profileValidator;
    }

    @Override
    public ProfileDto saveProfile(ProfileDto profileDto) throws ValidationException {
        LOGGER.trace("saveProfile({})", profileDto);

        profileValidator.validateForCreate(profileDto);

        //find all the allergens : TODO: validator here
        List<Allergene> allergenes = allergeneRepository.findAll();

        for (AllergeneDto allergeneDto : profileDto.getAllergens()) {
            //check if the allergens correspond to the ones in the database
            if (allergenes.stream().noneMatch(allergene -> allergene.getId() == allergeneDto.getId())) {
                throw new NotFoundException("Allergene with id " + allergeneDto.getId() + " does not exist");
            }
        }

        //find all the ingredients : TODO: validator here
        List<Ingredient> ingredients = ingredientRepository.findAll();

        for (IngredientDto ingredientDto : profileDto.getIngredient()) {
            //check if the ingredients correspond to the ones in the database
            if (ingredients.stream().noneMatch(ingredient -> ingredient.getId() == ingredientDto.getId())) {
                throw new NotFoundException("Ingredient with id " + ingredientDto.getId() + " does not exist");
            }
        }

        return profileMapper.profileToProfileDto(profileRepository.save(profileMapper.profileDtoToProfile(profileDto)));
    }
}
