package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.*;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.AllergeneMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.IngredientMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ProfileMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Allergene;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Ingredient;
import at.ac.tuwien.sepr.groupphase.backend.entity.Profile;
import at.ac.tuwien.sepr.groupphase.backend.entity.Recipe;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.AllergeneRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.IngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ProfileRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.ProfileService;
import at.ac.tuwien.sepr.groupphase.backend.service.validation.ProfileValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
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
    private AllergeneMapper allergeneMapper;
    private IngredientMapper ingredientMapper;

    public ProfileServiceImpl(ProfileRepository profileRepository, AllergeneRepository allergeneRepository,
                              ProfileMapper profileMapper, IngredientRepository ingredientRepository,
                              RecipeRepository recipeRepository, UserRepository userRepository,
                              ProfileValidator profileValidator, AllergeneMapper allergeneMapper, IngredientMapper ingredientMapper) {
        this.profileRepository = profileRepository;
        this.allergeneRepository = allergeneRepository;
        this.ingredientRepository = ingredientRepository;
        this.recipeRepository = recipeRepository;
        this.profileMapper = profileMapper;
        this.profileValidator = profileValidator;
        this.userRepository = userRepository;
        this.allergeneMapper = allergeneMapper;
        this.ingredientMapper = ingredientMapper;
    }

    @Override
    public ProfileSearchResultDto searchProfiles(ProfileSearchDto searchParams) {
        LOGGER.debug("search profiles");

        //Set Default values
        String name = Optional.ofNullable(searchParams)
            .map(ProfileSearchDto::name)
            .filter(str -> !str.trim().isEmpty())
            .orElse("");
        String creator = Optional.ofNullable(searchParams)
            .map(ProfileSearchDto::creator)
            .filter(str -> !str.trim().isEmpty())
            .orElse("");
        int pageSelector = Optional.ofNullable(searchParams)
            .map(ProfileSearchDto::page)
            .filter(page -> page >= 0)
            .orElse(0);
        int entriesPerPage = Optional.ofNullable(searchParams)
            .map(ProfileSearchDto::entriesPerPage)
            .filter(entries -> entries >= 21)
            .orElse(21);
        Long userId = Optional.ofNullable(searchParams)
            .map(ProfileSearchDto::userId)
            .filter(id -> id != 0)
            .orElse(null);

        Pageable page = PageRequest.of(pageSelector, entriesPerPage);
        Page<Profile> profiles = creator.isEmpty()
            ? profileRepository.findByNameContainingIgnoreCaseAndUserId(name, userId, page)
            : profileRepository.findByNameContainingIgnoreCaseAndCreatorAndNotUserId(name, creator, userId, page);

        List<ProfileListDto> profileDtos = profiles.getContent().stream()
            .map(profile -> new ProfileListDto(
                profile.getId(),
                profile.getName(),
                profile.getAllergens().stream().map(allergeneMapper::allergeneToAllergeneDto).toList(),
                profile.getIngredient().stream().map(ingredientMapper::ingredientToIngredientDto).toList(),
                profile.getUser().getId(),
                profile.getUser().getNickname()
            ))
            .toList();

        return new ProfileSearchResultDto(pageSelector, entriesPerPage, profiles.getTotalPages(), profileDtos);
    }


    @Override
    public ProfileDto saveProfile(ProfileDto profileDto) throws ValidationException, NotFoundException {
        LOGGER.trace("saveProfile({})", profileDto);

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
            if (!ratingProfile.getDisliked().add(recipeToRate)) {
                ratingProfile.getDisliked().remove(recipeToRate);
            }
        } else if (recipeRatingDto.rating() == 1) {
            ratingProfile.getDisliked().remove(recipeToRate);
            if (!ratingProfile.getLiked().add(recipeToRate)) {
                ratingProfile.getLiked().remove(recipeToRate);
            }
        }
        profileRepository.save(ratingProfile);
    }

    @Override
    public RecipeRatingListsDto getRatingLists(long id) throws NotFoundException {
        LOGGER.trace("getRatingLists({})", id);

        ApplicationUser user = userRepository.getReferenceById(id);

        Profile activeProfile = user.getActiveProfile();

        List<Long> liked = new ArrayList<>();
        List<Long> disliked = new ArrayList<>();

        for (Recipe recipe : activeProfile.getLiked()) {
            liked.add(recipe.getId());
        }

        for (Recipe recipe : activeProfile.getDisliked()) {
            disliked.add(recipe.getId());
        }

        RecipeRatingListsDto ratingLists = new RecipeRatingListsDto(
            liked,
            disliked);

        return ratingLists;
    }

    @Override
    public ProfileDetailDto getProfileDetails(long id) throws NotFoundException {
        LOGGER.trace("getProfileDetails({})", id);
        Optional<Profile> profileOptional = profileRepository.findById(id);
        Profile profile;
        if (profileOptional.isEmpty()) {
            throw new NotFoundException("Profile could not be found");
        } else {
            profile = profileOptional.get();
        }
        ArrayList<String> allergens = new ArrayList<>();
        if (profile.getAllergens() != null) {
            for (Allergene allergene : profile.getAllergens()) {
                allergens.add(allergene.getName());
            }
        }

        ArrayList<String> ingredients = new ArrayList<>();
        if (profile.getIngredient() != null) {
            for (Ingredient ingredient : profile.getIngredient()) {
                ingredients.add(ingredient.getName());
            }
        }

        ArrayList<RecipeProfileViewDto> liked = new ArrayList<>();
        for (Recipe recipe : profile.getLiked()) {
            liked.add(new RecipeProfileViewDto(recipe.getId(), recipe.getName()));
        }

        ArrayList<RecipeProfileViewDto> disliked = new ArrayList<>();
        for (Recipe recipe : profile.getDisliked()) {
            disliked.add(new RecipeProfileViewDto(recipe.getId(), recipe.getName()));
        }

        ProfileDetailDto profileDetails = new ProfileDetailDto(profile.getId(), profile.getName(),
            allergens, ingredients, liked, disliked, profile.getUser().getNickname(), profile.getUser().getId());

        return profileDetails;
    }

    @Override
    public ProfileDto editProfile(ProfileDto profileDto) throws ValidationException, NotFoundException {
        LOGGER.trace("editProfile({})", profileDto);

        profileValidator.validateForCreate(profileDto);

        Optional<Profile> profileToEditOp = profileRepository.findById(profileDto.getId());

        if (profileToEditOp.isEmpty()) {
            throw new NotFoundException("This profile does not exist in the database");
        }

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

        Profile profileToEdit = profileToEditOp.get();

        ProfileUserDto actualUser = new ProfileUserDto();
        actualUser.setId(profileDto.getId());
        actualUser.setName(profileDto.getName());
        actualUser.setIngredient(profileDto.getIngredient());
        actualUser.setAllergens(profileDto.getAllergens());
        actualUser.setUser(user.get());

        Profile editedProfile = profileMapper.profileDtoToProfile(actualUser);
        profileToEdit.setAllergens(editedProfile.getAllergens());
        profileToEdit.setIngredient(editedProfile.getIngredient());
        profileToEdit.setName(profileDto.getName());
        Profile edited = profileRepository.save(profileToEdit);
        ProfileDto editedDto = profileMapper.profileToProfileDto(edited);
        editedDto.setUserId(edited.getUser().getId());
        return editedDto;
    }
}
