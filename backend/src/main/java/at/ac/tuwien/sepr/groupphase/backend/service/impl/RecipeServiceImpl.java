package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe.RecipeDetailsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe.RecipeIngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe.RecipeListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe.RecipeSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe.RecipeSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Allergene;
import at.ac.tuwien.sepr.groupphase.backend.entity.AllergeneIngredient;
import at.ac.tuwien.sepr.groupphase.backend.entity.Ingredient;
import at.ac.tuwien.sepr.groupphase.backend.entity.Picture;
import at.ac.tuwien.sepr.groupphase.backend.entity.Recipe;
import at.ac.tuwien.sepr.groupphase.backend.entity.RecipeIngredient;
import at.ac.tuwien.sepr.groupphase.backend.entity.RecipeIngredientDetails;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.DataStoreException;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.AllergeneIngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeIngredientDetailsRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeIngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.PictureService;
import at.ac.tuwien.sepr.groupphase.backend.service.RecipeService;
import at.ac.tuwien.sepr.groupphase.backend.service.validation.RecipeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class RecipeServiceImpl implements RecipeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private RecipeRepository recipeRepository;
    private RecipeIngredientRepository recipeIngredientRepository;
    private RecipeIngredientDetailsRepository recipeIngredientDetailsRepository;
    private AllergeneIngredientRepository allergeneIngredientRepository;
    private RecipeValidator validator;
    private IngredientServiceImpl ingredientService;
    private JpaUserService userService;
    private PictureService pictureService;

    @Autowired
    public RecipeServiceImpl(RecipeRepository recipeRepository, RecipeIngredientRepository recipeIngredientRepository,
                             RecipeIngredientDetailsRepository recipeIngredientDetailsRepository, AllergeneIngredientRepository allergeneIngredientRepository,
                             RecipeValidator validator, IngredientServiceImpl ingredientService, JpaUserService userService, PictureService pictureService) {
        this.recipeRepository = recipeRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.recipeIngredientDetailsRepository = recipeIngredientDetailsRepository;
        this.allergeneIngredientRepository = allergeneIngredientRepository;
        this.validator = validator;
        this.ingredientService = ingredientService;
        this.userService = userService;
        this.pictureService = pictureService;
    }

    @Override
    public RecipeSearchResultDto searchRecipes(RecipeSearchDto searchParams) {
        LOGGER.debug("search recipes");

        String name = "";
        int pageSelector = 0;
        int entriesPerPage = 21;

        // checking searchParams
        if (searchParams != null) {
            name = !searchParams.name().trim().isEmpty() ? searchParams.name() : "";

            if (searchParams.page() >= 0) {
                pageSelector = searchParams.page();
            }

            if (searchParams.entriesPerPage() >= 22) {
                entriesPerPage = searchParams.entriesPerPage();
            }
        }

        Pageable page = PageRequest.of(pageSelector, entriesPerPage);
        Page<Recipe> recipes = this.recipeRepository.findByNameContainingIgnoreCase(name, page);

        ArrayList<RecipeListDto> recipeDtos = new ArrayList<>();
        for (Recipe recipe : recipes) {
            String nickname = "BiteBuddy";

            if (recipe.getCreatorId() != null && recipe.getCreatorId() > 0) {
                nickname = this.userService.getUserById(recipe.getCreatorId()).getNickname();
            }

            recipeDtos.add(new RecipeListDto(nickname, recipe.getName(), recipe.getId(), recipe.getPictureId()));
        }

        return new RecipeSearchResultDto(pageSelector, entriesPerPage, recipes.getTotalPages(), recipeDtos);
    }

    @Override
    public List<Recipe> getAll() {
        return this.recipeRepository.findAll();
    }

    @Override
    public List<Recipe> getAllWithoutAllergens(Set<Allergene> allergens) {
        Set<Long> allergeneIds = new HashSet<>();
        for (Allergene a : allergens) {
            allergeneIds.add(a.getId());
        }
        // fix a problem with SQL where empty lists will always return empty result set
        if (allergeneIds.isEmpty()) {
            allergeneIds.add(Long.MAX_VALUE); // there should never be any allergen with this ID
        }
        return this.recipeRepository.getAllWithoutAllergens(allergeneIds);
    }

    @Override
    public List<Recipe> getAllWithIngredientsWithoutAllergens(Set<String> ingredientNames, Set<Allergene> allergens) {
        Set<Long> allergeneIds = new HashSet<>();
        for (Allergene a : allergens) {
            allergeneIds.add(a.getId());
        }
        // fix a problem with SQL where empty lists will always return empty result set
        if (allergeneIds.isEmpty()) {
            allergeneIds.add(Long.MAX_VALUE); // there should never be any allergen with this ID
        }

        if (ingredientNames.isEmpty()) {
            ingredientNames.add("-");
        }

        return this.recipeRepository.getAllWithIngredientsWithoutAllergens(ingredientNames, allergeneIds);
    }


    @Override
    public void createRecipe(RecipeDetailsDto recipe, Long userId) throws ConflictException, ValidationException {
        LOGGER.debug("createRecipe({}, {})", recipe, userId);

        // validate recipe
        this.validator.validateForCreate(recipe);

        // creating picture
        Long pictureId = null;
        if (recipe.picture() != null) {
            try {
                Picture pic = this.pictureService.createPicture(recipe.picture());
                pictureId = pic.getId();
            } catch (DataStoreException e) {
                LOGGER.warn("Image sent was not valid");
            }
        }

        ArrayList<String> conflictList = new ArrayList<>();
        Set<RecipeIngredient> ingredients = new HashSet<>();

        // creating database entry
        Recipe newRecipe = new Recipe();
        newRecipe.setPictureId(pictureId);
        newRecipe.setName(recipe.name());
        newRecipe.setInstructions(recipe.description());
        newRecipe.setIngredients(ingredients);
        newRecipe.setCreatorId(userId);
        this.recipeRepository.save(newRecipe);

        // getting recipe id & checking if we can find the RecipeIngredients
        Recipe queriedRecipe = this.recipeRepository.findByNameContainingIgnoreCase(recipe.name()).get(0);

        // update recipe with the correct ingredients
        ingredients = new HashSet<>();
        for (RecipeIngredientDto ingredient : recipe.ingredients()) {
            List<Ingredient> queriedResults = this.ingredientService.getByNameMatching(ingredient.name());

            if (!queriedResults.isEmpty()) {
                RecipeIngredientDetails r = new RecipeIngredientDetails();
                r.setDescriber("test Describer");
                r.setUnit(ingredient.unit());
                r.setIngredient(ingredient.name());
                r.setAmount(ingredient.amount());

                RecipeIngredient ing = new RecipeIngredient();
                ing.setAmount(r);
                ing.setIngredient(queriedResults.get(0));
                ing.setRecipe(queriedRecipe);
                ingredients.add(ing);
                this.recipeIngredientDetailsRepository.save(r);
                this.recipeIngredientRepository.save(ing);
            } else {
                conflictList.add("Ingredient " + ingredient.name() + "does not exist");
            }
        }

        // checking that each recipe actually exists
        // (later when amount and everything is implemented the validation needs to be more complex and broad)
        if (!conflictList.isEmpty()) {
            this.recipeRepository.delete(queriedRecipe);
            throw new ConflictException("Ingredients do not match with the database", conflictList);
        }

        this.recipeRepository.updateIngredients(queriedRecipe.getId(), ingredients);
    }

    @Override
    public long getHighestRecipeId() {
        LOGGER.trace("getHighestRecipeId()");

        Recipe r = this.recipeRepository.findFirstByOrderByIdDesc();
        return r.getId();
    }

    @Override
    public long getLowestRecipeId() {
        LOGGER.trace("getLowestRecipeId()");

        Recipe r = this.recipeRepository.findFirstByOrderByIdAsc();
        return r.getId();
    }

    @Override
    public Recipe getRecipeById(long id) throws NotFoundException {
        LOGGER.trace("getRecipeById({})", id);

        Optional<Recipe> recipe = this.recipeRepository.findById(id);
        if (recipe.isEmpty()) {
            throw new NotFoundException("The searched for recipe does not exist in the database.");
        } else {
            return recipe.get();
        }
    }

    @Override
    public List<String> findMatchingIngredients(String name) {
        LOGGER.trace("findMatchingIngredients({})", name);

        ArrayList<String> matchingIngredients = new ArrayList<>();
        List<Ingredient> ingredients = this.ingredientService.getByNameMatching(name);
        List<RecipeIngredientDetails> detailedIngredients = this.recipeIngredientDetailsRepository.findByIngredientContainingIgnoreCase(name);

        for (int i = 0; i < 5; i++) {
            if (ingredients.size() > i) {
                matchingIngredients.add(ingredients.get(i).getName());
            }

            if (detailedIngredients.size() > i) {
                matchingIngredients.add(detailedIngredients.get(i).getIngredient());
            }
        }
        return matchingIngredients.stream().sorted().toList();
    }

    @Override
    public List<RecipeIngredient> findMatchingRecipeIngredients(String name) {
        List<RecipeIngredientDetails> rids = this.recipeIngredientDetailsRepository.findByIngredientContainingIgnoreCase(name);
        List<RecipeIngredient> results = new ArrayList<>();

        for (RecipeIngredientDetails details : rids) {
            List<RecipeIngredient> queryResult = this.recipeIngredientRepository.findByAmountEquals(details);

            if (!queryResult.isEmpty()) {
                results.add(queryResult.get(0));
            }
        }

        return results;
    }

    @Override
    public void createRating(long recipeId, long userId, int rating) {
        LOGGER.trace("createRating({})({})", recipeId, userId);

    }

    @Override
    public RecipeDetailsDto getDetailedRecipe(long id) throws UserNotFoundException {

        LOGGER.trace("details({})", id);
        Optional<Recipe> recipe = this.recipeRepository.findById(id);
        if (recipe.isEmpty()) {
            throw new NotFoundException("The searched for recipe does not exist in the database anymore.");
        } else {
            List<RecipeIngredient> ingredients = this.recipeIngredientRepository.findByRecipe(recipe.get());
            ArrayList<String> ingredientsAndAmount = new ArrayList<>();
            ArrayList<RecipeIngredientDto> newIngredients = new ArrayList<>();
            for (RecipeIngredient ingredient : ingredients) {
                Ingredient currentIngredient = ingredient.getIngredient();
                ingredientsAndAmount.add(currentIngredient.getName() + ": " + ingredient.getAmount());

                newIngredients.add(new RecipeIngredientDto(
                    ingredient.getAmount().getIngredient(),
                    ingredient.getAmount().getAmount(),
                    ingredient.getAmount().getUnit()));
            }

            if (newIngredients.isEmpty()) {
                throw new NotFoundException("The searched for recipe does not have any ingredients");
            } else {
                ArrayList<String> allergens = new ArrayList<>();
                for (RecipeIngredient recipeIngredient : ingredients) {
                    List<AllergeneIngredient> allergensIngredient = this.allergeneIngredientRepository.findByIngredient(recipeIngredient.getIngredient());
                    for (AllergeneIngredient allergene : allergensIngredient) {
                        if (!allergens.contains(allergene.getAllergene().getName())) {
                            allergens.add(allergene.getAllergene().getName());
                        }
                    }
                }

                String creatorName = "BiteBuddy";
                if (recipe.get().getCreatorId() >= 0) {
                    ApplicationUser creator = this.userService.getUserById(recipe.get().getCreatorId());
                    creatorName = creator.getNickname();
                }

                RecipeDetailsDto detailsDto =
                    new RecipeDetailsDto(id, recipe.get().getName(), creatorName, recipe.get().getInstructions(), newIngredients, allergens,
                        recipe.get().getPictureId(), null);
                return detailsDto;
            }
        }

    }
}

