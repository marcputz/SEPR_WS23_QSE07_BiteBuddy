package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.AllergeneDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.IngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileUserDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeGetByIdDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeRatingDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeRatingListsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.AllergeneMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.IngredientMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ProfileMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.RecipeMapper;
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
import java.util.stream.Collectors;

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
    private final RecipeMapper recipeMapper;

    public ProfileServiceImpl(ProfileRepository profileRepository, AllergeneRepository allergeneRepository,
                              ProfileMapper profileMapper, IngredientRepository ingredientRepository,
                              RecipeRepository recipeRepository, UserRepository userRepository,
                              ProfileValidator profileValidator, AllergeneMapper allergeneMapper, IngredientMapper ingredientMapper,
                              RecipeMapper recipeMapper) {
        this.profileRepository = profileRepository;
        this.allergeneRepository = allergeneRepository;
        this.ingredientRepository = ingredientRepository;
        this.recipeRepository = recipeRepository;
        this.profileMapper = profileMapper;
        this.profileValidator = profileValidator;
        this.userRepository = userRepository;
        this.recipeMapper = recipeMapper;
    }

    @Override
    public ProfileSearchResultDto searchProfiles(ProfileSearchDto searchParams, Long currentUserId) {
        LOGGER.debug("search profiles");

        String name = (searchParams.name() != null && !searchParams.name().trim().isEmpty()) ? searchParams.name() : "";

        String creator = (searchParams.creator() != null && !searchParams.creator().trim().isEmpty()) ? searchParams.creator() : "";

        int pageSelector = Math.max(searchParams.page(), 0);

        int entriesPerPage = Math.max(searchParams.entriesPerPage(), 21);

        Pageable page = PageRequest.of(pageSelector, entriesPerPage);
        Page<Profile> profilesPage = searchParams.ownProfiles()
            ? profileRepository.findByNameContainingIgnoreCaseAndUserId(name, currentUserId, page)
            : profileRepository.findByNameContainingIgnoreCaseAndCreatorAndNotUserId(name, creator, currentUserId, page);
        List<Profile> profiles = profilesPage.getContent().stream().toList();
        List<ProfileDetailDto> profileDtos = profiles.stream()
            .map(profile -> new ProfileDetailDto(
                profile.getId(),
                profile.getName(),
                profile.getAllergens().stream().map(Allergene::getName).collect(Collectors.toCollection(ArrayList::new)),
                profile.getIngredient().stream().map(Ingredient::getName).collect(Collectors.toCollection(ArrayList::new)),
                profile.getLiked().stream().map(recipeMapper::recipeToRecipeGetByIdDto).collect(Collectors.toCollection(ArrayList::new)),
                profile.getDisliked().stream().map(recipeMapper::recipeToRecipeGetByIdDto).collect(Collectors.toCollection(ArrayList::new)),
                profile.getUser().getNickname(),
                profile.getUser().getId()
            ))
            .toList();

        return new ProfileSearchResultDto(pageSelector, entriesPerPage, profilesPage.getTotalPages(), profileDtos);
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

        Optional<ApplicationUser> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new NotFoundException("The user does not exist");
        }

        Profile activeProfile = user.get().getActiveProfile();

        if (activeProfile == null) {
            throw new NotFoundException("The user does not have an active profile");
        }

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

        ArrayList<RecipeGetByIdDto> liked = new ArrayList<>();
        for (Recipe recipe : profile.getLiked()) {
            liked.add(new RecipeGetByIdDto(recipe.getId(), recipe.getName()));
        }

        ArrayList<RecipeGetByIdDto> disliked = new ArrayList<>();
        for (Recipe recipe : profile.getDisliked()) {
            disliked.add(new RecipeGetByIdDto(recipe.getId(), recipe.getName()));
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


        ProfileUserDto actualUser = new ProfileUserDto();
        actualUser.setId(profileDto.getId());
        actualUser.setName(profileDto.getName());
        actualUser.setIngredient(profileDto.getIngredient());
        actualUser.setAllergens(profileDto.getAllergens());
        actualUser.setUser(user.get());

        Profile profileToEdit = profileToEditOp.get();
        Profile editedProfile = profileMapper.profileDtoToProfile(actualUser);
        profileToEdit.setAllergens(editedProfile.getAllergens());
        profileToEdit.setIngredient(editedProfile.getIngredient());
        profileToEdit.setName(profileDto.getName());
        Profile edited = profileRepository.save(profileToEdit);
        ProfileDto editedDto = profileMapper.profileToProfileDto(edited);
        editedDto.setUserId(edited.getUser().getId());
        editedDto.setId(edited.getId());
        return editedDto;
    }
}
