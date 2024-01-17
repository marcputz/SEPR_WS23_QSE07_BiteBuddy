package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.*;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ProfileMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.*;
import at.ac.tuwien.sepr.groupphase.backend.service.ProfileService;
import at.ac.tuwien.sepr.groupphase.backend.service.validation.ProfileValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;

@Service
public class ProfileServiceImpl implements ProfileService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final ProfileValidator profileValidator;
    private final AllergeneRepository allergeneRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    public ProfileServiceImpl(ProfileRepository profileRepository, AllergeneRepository allergeneRepository,
                              ProfileMapper profileMapper, IngredientRepository ingredientRepository,
                              RecipeRepository recipeRepository, UserRepository userRepository,
                              ProfileValidator profileValidator) {
        this.profileRepository = profileRepository;
        this.allergeneRepository = allergeneRepository;
        this.ingredientRepository = ingredientRepository;
        this.recipeRepository = recipeRepository;
        this.profileMapper = profileMapper;
        this.profileValidator = profileValidator;
        this.userRepository = userRepository;
    }

    @Override
    public ProfileDto saveProfile(ProfileDto profileDto) throws ValidationException {
        LOGGER.trace("saveProfile({})", profileDto);
        LOGGER.info("saveProfile({})", profileDto);

        profileValidator.validateForCreate(profileDto);

        //check if the allergens correspond to the ones in the database
        List<Allergene> allergenes = allergeneRepository.findAll();
        for (AllergeneDto allergeneDto : profileDto.getAllergens()) {
            if (allergenes.stream().noneMatch(allergene -> allergene.getId() == allergeneDto.getId())) {
                throw new NotFoundException("Allergene with id " + allergeneDto.getId() + " does not exist");
            }
        }

        //check if the ingredients correspond to the ones in the database
        List<Ingredient> ingredients = ingredientRepository.findAll();
        for (IngredientDto ingredientDto : profileDto.getIngredient()) {
            if (ingredients.stream().noneMatch(ingredient -> ingredient.getId() == ingredientDto.getId())) {
                throw new NotFoundException("Ingredient with id " + ingredientDto.getId() + " does not exist");
            }
        }

        Optional<ApplicationUser> user = userRepository.findById(profileDto.getUserId());
        if (user.isEmpty()) {
            throw new NotFoundException("User with id " + profileDto.getUserId() + " does not exist");
        }

        LOGGER.info("GOT RIGHT BEFORE POSTING User is: " + user.get().getId());

        ProfileUserDto actualUser = new ProfileUserDto();
        actualUser.setName(profileDto.getName());
        actualUser.setIngredient(profileDto.getIngredient());
        actualUser.setAllergens(profileDto.getAllergens());
        actualUser.setUser(user.get());


        Profile created = profileRepository.save(profileMapper.profileDtoToProfile(actualUser));
        ProfileDto createdDto = profileMapper.profileToProfileDto(created);
        createdDto.setUserId(created.getUser().getId());
        user.get().setActiveProfile(created);
        userRepository.save(user.get());
        return createdDto;
        //return profileMapper.profileToProfileDto(profileRepository.save(profileMapper.profileDtoToProfile(actualUser)));
    }

    public void rateRecipe(RecipeRatingDto recipeRatingDto) throws NotFoundException, ValidationException {
        LOGGER.trace("createRating({})", recipeRatingDto);

        Recipe recipeToRate = recipeRepository.getReferenceById(recipeRatingDto.recipeId());
        ApplicationUser user = userRepository.getReferenceById(recipeRatingDto.userId());
        Profile ratingProfile = user.getActiveProfile();
        profileValidator.validateRating(recipeRatingDto.rating());



        if (recipeRatingDto.rating() == 0) {
            ratingProfile.getLiked().remove(recipeToRate);
            ratingProfile.getDisliked().add(recipeToRate);
        } else if (recipeRatingDto.rating() == 1) {
            ratingProfile.getDisliked().remove(recipeToRate);
            ratingProfile.getLiked().add(recipeToRate);
        }
        profileRepository.save(ratingProfile);
    }
}
