package at.ac.tuwien.sepr.groupphase.backend.service.impl;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InventoryIngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InventoryListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe.RecipeListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan.MenuPlanContentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan.MenuPlanDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan.MenuPlanUpdateRecipeDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Allergene;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Ingredient;
import at.ac.tuwien.sepr.groupphase.backend.entity.InventoryIngredient;
import at.ac.tuwien.sepr.groupphase.backend.entity.MenuPlan;
import at.ac.tuwien.sepr.groupphase.backend.entity.MenuPlanContent;
import at.ac.tuwien.sepr.groupphase.backend.entity.Profile;
import at.ac.tuwien.sepr.groupphase.backend.entity.Recipe;
import at.ac.tuwien.sepr.groupphase.backend.entity.RecipeIngredient;
import at.ac.tuwien.sepr.groupphase.backend.entity.idclasses.MenuPlanContentId;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.DataStoreException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.InventoryIngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.MenuPlanRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeIngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.IngredientService;
import at.ac.tuwien.sepr.groupphase.backend.service.MenuPlanService;
import at.ac.tuwien.sepr.groupphase.backend.service.RecipeService;
import at.ac.tuwien.sepr.groupphase.backend.service.validation.MenuPlanValidator;
import org.apache.commons.lang3.NotImplementedException;
import org.hibernate.JDBCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * JPA implementation of MenuPlanService interface.
 *
 * @author Marc Putz
 */
@Service
public class JpaMenuPlanService implements MenuPlanService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final MenuPlanValidator validator;

    private final RecipeService recipeService;
    private final IngredientService ingredientService;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final MenuPlanRepository menuPlanRepository;
    private final InventoryIngredientRepository inventoryIngredientRepository;

    @Autowired
    public JpaMenuPlanService(MenuPlanRepository repository, RecipeService recipeService, IngredientService ingredientService, MenuPlanValidator validator,
                              RecipeIngredientRepository recipeIngredientRepository, InventoryIngredientRepository inventoryIngredientRepository) {
        this.validator = validator;
        this.menuPlanRepository = repository;
        this.recipeService = recipeService;
        this.ingredientService = ingredientService;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.inventoryIngredientRepository = inventoryIngredientRepository;
    }

    @Override
    public MenuPlan getById(long id) throws NotFoundException {
        Optional<MenuPlan> dbVal = menuPlanRepository.findById(id);
        if (dbVal.isEmpty()) {
            throw new NotFoundException("MenuPlan with ID " + id + " could not be found in the data store.");
        } else {
            return dbVal.get();
        }
    }

    @Override
    public List<MenuPlan> getAllMenuPlansOfUser(ApplicationUser user) {
        return menuPlanRepository.getAllByUser(user);
    }

    @Override
    public List<MenuPlanDetailDto> getAllMenuPlansofUserDetailDto(ApplicationUser user) {
        List<MenuPlan> plans = menuPlanRepository.getAllByUser(user);
        List<MenuPlanDetailDto> details = new ArrayList<>();
        for (MenuPlan plan : plans) {
            MenuPlanDetailDto detail = new MenuPlanDetailDto();
            //Set<MenuPlanContentDetailDto> contents = getContentsOfMenuPlanAsDetailDto(plan);
            detail.setUserId(user.getId()).setFromTime(plan.getFromDate());  // .setContents(contents)
            detail.setUntilTime(plan.getUntilDate()).setNumDays(7).setId(plan.getId());
            details.add(detail);
        }
        return details;
    }

    @Override
    public MenuPlanDetailDto getMenuPlanForUserOnDateDetailDto(ApplicationUser user, LocalDate date) {
        MenuPlanDetailDto menuPlanDetailDto = new MenuPlanDetailDto();
        MenuPlan menuPlan = getMenuPlanForUserOnDate(user, date);
        if (menuPlan == null) {
            LOGGER.info("no Menuplan at this time: " + date.toString() + " and user id: " + user.getId() + " email: " + user.getEmail() + " nickname: " + user.getNickname() + " password: " + user.getPasswordEncoded());
            return null;
        }
        Set<MenuPlanContentDetailDto> contents = getContentsOfMenuPlanAsDetailDto(menuPlan);
        menuPlanDetailDto.setUserId(user.getId()).setContents(contents).setFromTime(menuPlan.getFromDate());
        menuPlanDetailDto.setUntilTime(menuPlan.getUntilDate()).setId(menuPlan.getId());
        LOGGER.info("get menuplan: " + menuPlanDetailDto.toString());
        return menuPlanDetailDto;
    }

    @Override
    public List<MenuPlan> getAllMenuPlansOfUserDuringTimeframe(ApplicationUser user, LocalDate from, LocalDate until) {
        return menuPlanRepository.getAllByUserMatchingTimeframe(user, from, until);
    }

    @Override
    public MenuPlan getMenuPlanForUserOnDate(ApplicationUser user, LocalDate date) {
        return menuPlanRepository.getByUserOnDate(user, date);
    }

    @Override
    public MenuPlanDetailDto generateContent(MenuPlan plan) throws DataStoreException, ConflictException {
        LOGGER.trace("generateContent({})", plan);

        /* algorithm parameters */
        // this is the chance a recipe with owned ingredients is selected
        final double chanceOwnedRecipeSelected = 0.6;
        // these are the chances to use either liked recipes or a recipes with preferred ingredients
        // both of these should be < 1
        final double chanceLikedRecipeSelected = 0.15;
        final double chancePreferredRecipeSelected = 0.5;

        if ((chanceLikedRecipeSelected + chancePreferredRecipeSelected) >= 1.0) {
            throw new IllegalArgumentException("Chances add up to more than 100%");
        }

        // make sure plan parameter is not NULL
        if (plan == null) {
            throw new IllegalArgumentException("MenuPlan parameter cannot be NULL value");
        }

        // get fridge contents
        Set<String> fridgeIngredientsNames = new HashSet<>(inventoryIngredientRepository.getOwnedIngredientsByMenuPlanId(plan.getId()));

        // get allergenes
        Profile profile = plan.getProfile();
        Set<Allergene> allergens = profile.getAllergens();
        // get profile ingredient preferences
        Set<Ingredient> likedIngredients = profile.getIngredient();
        Set<String> likedIngredientsNames = new HashSet<>();
        for (Ingredient i : likedIngredients) {
            likedIngredientsNames.add(i.getName());
        }

        // get disliked recipes
        Set<Recipe> dislikedRecipes = profile.getDisliked();
        // get liked recipes
        Set<Recipe> likedRecipes = profile.getLiked();
        // get available recipes without allergens from data store
        List<Recipe> availableRecipes = this.recipeService.getAllWithoutAllergens(allergens);
        // get recipes with preferred ingredients from data store
        List<Recipe> preferredRecipes = this.recipeService.getAllWithIngredientsWithoutAllergens(likedIngredientsNames, allergens);
        // get recipes with owned ingredients from data store
        List<Recipe> ownedRecipes = this.recipeService.getAllWithIngredientsWithoutAllergens(fridgeIngredientsNames, allergens);

        // filter lists for disliked recipes
        availableRecipes = availableRecipes.stream().filter(r -> !dislikedRecipes.contains(r)).toList();
        preferredRecipes = preferredRecipes.stream().filter(r -> !dislikedRecipes.contains(r)).toList();
        ownedRecipes = ownedRecipes.stream().filter(r -> !dislikedRecipes.contains(r)).toList();

        // combine lists
        List<Recipe> ownedAndLikedRecipes = new ArrayList<>();
        for (Recipe r : ownedRecipes) {
            if (likedRecipes.contains(r)) {
                ownedAndLikedRecipes.add(r);
            }
        }
        List<Recipe> ownedAndPreferredRecipes = new ArrayList<>();
        for (Recipe r : ownedRecipes) {
            if (preferredRecipes.contains(r)) {
                ownedAndPreferredRecipes.add(r);
            }
        }

        // define content sets
        Set<MenuPlanContent> contents = new HashSet<>();
        Set<MenuPlanContentDetailDto> contentDtos = new HashSet<>();

        // select recipes from list and populate content lists
        int numDays = (int) Duration.between(plan.getFromDate().atStartOfDay(), plan.getUntilDate().atStartOfDay()).toDays() + 1;
        final int timeslotsPerDay = 3; // TODO: make this changeable
        // set to keep track of already added recipes, so nothing is added twice
        Set<Long> excludedIds = new HashSet<>();
        // generate content for each day and each timeslot
        for (int day = 0; day < numDays; day++) {
            for (int timeslot = 0; timeslot < timeslotsPerDay; timeslot++) {

                Recipe selectedRecipe = null;

                // determine which recipe list to use
                List<Recipe> recipeList = null;
                boolean useOwnedRecipes = ThreadLocalRandom.current().nextDouble(0.0, 1.0) < chanceOwnedRecipeSelected;
                boolean useLikedRecipes = ThreadLocalRandom.current().nextDouble(0.0, 1.0) < chanceLikedRecipeSelected;
                boolean usePreferredRecipes = ThreadLocalRandom.current().nextDouble(0.0, 1.0) < chancePreferredRecipeSelected;

                if (useOwnedRecipes && !ownedRecipes.isEmpty()) {
                    if (useLikedRecipes && !ownedAndLikedRecipes.isEmpty()) {
                        recipeList = ownedAndLikedRecipes;
                    } else {
                        if (usePreferredRecipes && !ownedAndPreferredRecipes.isEmpty()) {
                            recipeList = ownedAndPreferredRecipes;
                        } else {
                            recipeList = ownedRecipes;
                        }
                    }
                } else {
                    if (useLikedRecipes && !likedRecipes.isEmpty()) {
                        recipeList = new ArrayList<>(likedRecipes);
                    } else {
                        if (usePreferredRecipes && !preferredRecipes.isEmpty()) {
                            recipeList = preferredRecipes;
                        } else {
                            recipeList = availableRecipes;
                        }
                    }
                }

                // if recipe list is empty, there are no recipes available
                if (recipeList.isEmpty()) {
                    throw new ConflictException("Cannot generate menu plan", List.of("Menu Plan can not find enough content in data store"));
                }

                // try to get a random recipe from the recipeList
                final int maxRetries = 20;
                for (int retry = 0; selectedRecipe == null && retry < maxRetries; retry++) {
                    // generate random index and get from list
                    int recipeIdx = ThreadLocalRandom.current().nextInt(0, recipeList.size());
                    selectedRecipe = recipeList.get(recipeIdx);

                    // check if recipe is already used
                    if (excludedIds.contains(selectedRecipe.getId())) {
                        selectedRecipe = null;
                        continue;
                    }

                    excludedIds.add(selectedRecipe.getId());
                }

                // if no recipe could be found, try again with all available recipes
                if (selectedRecipe == null) {
                    for (int retry = 0; selectedRecipe == null && retry < maxRetries; retry++) {
                        // generate random index and get from list
                        int recipeIdx = ThreadLocalRandom.current().nextInt(0, availableRecipes.size());
                        selectedRecipe = availableRecipes.get(recipeIdx);

                        // check if recipe is already used
                        if (excludedIds.contains(selectedRecipe.getId())) {
                            selectedRecipe = null;
                            continue;
                        }

                        excludedIds.add(selectedRecipe.getId());
                    }

                    // if again nothing was found, there is not enough content
                    if (selectedRecipe == null) {
                        throw new ConflictException("Cannot generate menu plan", List.of("Menu Plan can not find enough content in data store"));
                    }
                }

                // create content entity and add to list
                MenuPlanContent content = new MenuPlanContent()
                    .setRecipe(selectedRecipe)
                    .setTimeslot(timeslot)
                    .setDayIdx(day);
                contents.add(content);

                // create content detail dto
                MenuPlanContentDetailDto contentDto = new MenuPlanContentDetailDto()
                    .setDay(day)
                    .setTimeslot(timeslot)
                    .setRecipe(new RecipeListDto(
                        "",
                        selectedRecipe.getName(),
                        selectedRecipe.getId(),
                        selectedRecipe.getPictureId()
                    ));
                contentDtos.add(contentDto);

            }
        }

        // add contents to menu plan
        for (MenuPlanContent c : contents) {
            plan.addContent(c);
        }

        try {

            // save menu plan
            MenuPlan savedPlan = this.menuPlanRepository.save(plan);

            // creating inventory
            this.createInventory(plan.getUser());

            // create detail dto and return
            return new MenuPlanDetailDto()
                .setUserId(savedPlan.getId())
                .setUntilTime(savedPlan.getUntilDate())
                .setFromTime(savedPlan.getFromDate())
                .setProfileId(savedPlan.getProfile().getId())
                .setProfileName(savedPlan.getProfile().getName())
                .setNumDays((int) Duration.between(
                    savedPlan.getFromDate().atStartOfDay(),
                    savedPlan.getUntilDate().atStartOfDay()
                ).toDays() + 1)
                .setContents(contentDtos);

        } catch (JDBCException e) {
            throw new DataStoreException(e.getErrorMessage(), e);
        }
    }

    @Override
    public MenuPlan createEmptyMenuPlan(ApplicationUser user, Profile profile, LocalDate from, LocalDate until)
        throws DataStoreException, ConflictException, ValidationException {
        LOGGER.trace("createEmptyMenuPlan({},{},{},{})", user, profile, from, until);

        validator.validateForCreate(user, profile, from, until);

        // check if there are already existing menu plans in the specified timeframe
        List<MenuPlan> conflictingMenuPlans = this.getAllMenuPlansOfUserDuringTimeframe(user, from, until);
        if (!conflictingMenuPlans.isEmpty()) {
            throw new ConflictException("New Menu Plan would conflict with the current system state",
                List.of("There is already a menu plan active during the specified timeframe"));
        }

        MenuPlan menuPlan = new MenuPlan()
            .setFromDate(from)
            .setUntilDate(until)
            .setUser(user)
            .setProfile(profile);

        try {
            return this.menuPlanRepository.save(menuPlan);
        } catch (JDBCException | DataIntegrityViolationException e) {
            throw new DataStoreException("Unable to create new MenuPlan entity", e);
        }
    }

    @Override
    public void deleteMenuPlan(MenuPlan toDelete) throws DataStoreException {
        deleteMenuPlan(toDelete.getId());
    }

    @Override
    public void deleteMenuPlan(long id) throws DataStoreException {
        try {
            this.menuPlanRepository.deleteById(id);
        } catch (JDBCException e) {
            throw new DataStoreException(e.getErrorMessage(), e);
        }
    }


    @Override
    public MenuPlan updateMenuPlanByChangingOneRecipe(ApplicationUser user, MenuPlanUpdateRecipeDto updateDto) {

        // the menuplan only has 1 Content which is the one to be changed
        LOGGER.trace("updateMenuPlanByChangingOneRecepy({},{})", user, updateDto);

        MenuPlan oldPlan = this.getById(updateDto.getMenuPlanId());
        for (MenuPlanContent content : oldPlan.getContent()) {
            if (content.getDayIdx() == updateDto.getDay() && content.getTimeslot() == updateDto.getTimeslot()) {
                Set<Allergene> allergenes = user.getActiveProfile().getAllergens();
                List<Recipe> recipes = recipeService.getAllWithoutAllergens(allergenes);
                Random random = new Random();
                LOGGER.trace("before randomIndex size of recepys: " + recipes.size());
                Set<Recipe> oldDisliked = user.getActiveProfile().getDisliked();

                int randomIndex = random.nextInt(recipes.size());
                Recipe randomRecipe = recipes.get(randomIndex);
                while (Objects.equals(content.getRecipe().getId(), randomRecipe.getId()) || oldDisliked.contains(randomRecipe)) {
                    randomIndex = random.nextInt(recipes.size());
                    randomRecipe = recipes.get(randomIndex);
                }
                content.setRecipe(randomRecipe);
                LOGGER.trace("before dislike");
                if (updateDto.isDislike()) {
                    Set<Recipe> disliked = user.getActiveProfile().getDisliked();
                    disliked.add(content.getRecipe());
                    user.getActiveProfile().setDisliked(disliked);
                }
            }
        }

        menuPlanRepository.save(oldPlan);
        return oldPlan;
    }

    @Override
    public MenuPlanDetailDto updateMenuPlan(MenuPlan toUpdate) throws DataStoreException, ValidationException, ConflictException {
        // validate input
        validator.validateForUpdate(toUpdate);

        // check if there are already existing menu plans in the specified timeframe
        List<MenuPlan> conflictingMenuPlans = this.getAllMenuPlansOfUserDuringTimeframe(toUpdate.getUser(), toUpdate.getFromDate(), toUpdate.getUntilDate());
        if (!conflictingMenuPlans.isEmpty()) {
            throw new ConflictException("New Menu Plan would conflict with the current system state",
                List.of("There is already a menu plan active during the specified timeframe"));
        }

        try {
            MenuPlan savedPlan = this.menuPlanRepository.save(toUpdate);

            // create dtos for menu plan content
            Set<MenuPlanContentDetailDto> contentDtos = this.convertContentsToDetailDtos(savedPlan.getContent());

            // create detail dto
            return new MenuPlanDetailDto()
                .setUserId(savedPlan.getId())
                .setUntilTime(savedPlan.getUntilDate())
                .setFromTime(savedPlan.getFromDate())
                // TODO: add profile information to detail dto
                .setProfileId(-1L)
                .setProfileName("Not available")
                .setNumDays((int) Duration.between(
                    savedPlan.getFromDate().atStartOfDay(),
                    savedPlan.getUntilDate().atStartOfDay()
                ).toDays() + 1)
                .setContents(contentDtos);
        } catch (JDBCException e) {
            throw new DataStoreException(e.getErrorMessage(), e);
        }
    }

    @Override
    public Set<MenuPlanContent> getContentsOfMenuPlan(MenuPlan plan) throws NotFoundException {
        return getContentsOfMenuPlan(plan.getId());
    }

    @Override
    public Set<MenuPlanContent> getContentsOfMenuPlan(long menuPlanId) throws NotFoundException {
        Optional<MenuPlan> dbVal = menuPlanRepository.findById(menuPlanId);
        if (dbVal.isEmpty()) {
            throw new NotFoundException("Menu Plan with ID '" + menuPlanId + "' does not exist in the data store.");
        }
        return dbVal.get().getContent() != null ? dbVal.get().getContent() : new HashSet<>();
    }

    @Override
    public Set<MenuPlanContentDetailDto> getContentsOfMenuPlanAsDetailDto(MenuPlan plan) throws NotFoundException {
        return getContentsOfMenuPlanAsDetailDto(plan.getId());
    }

    @Override
    public Set<MenuPlanContentDetailDto> getContentsOfMenuPlanAsDetailDto(long menuPlanId) throws NotFoundException {
        Set<MenuPlanContent> contents = this.getContentsOfMenuPlan(menuPlanId);
        return this.convertContentsToDetailDtos(contents);
    }

    @Override
    public Set<MenuPlanContent> getContentsOfMenuPlanOnDay(MenuPlan plan, int day) throws NotFoundException, IllegalArgumentException {
        int menuPlanDuration = (int) Duration.between(plan.getFromDate().atStartOfDay(), plan.getUntilDate().atStartOfDay()).toDays() + 1;
        if (menuPlanDuration >= day) {
            throw new IllegalArgumentException("Specified Day Index of '" + day + "' is outside the range of this menu plan");
        }

        Set<MenuPlanContent> allContent = this.getContentsOfMenuPlan(plan);

        Set<MenuPlanContent> contentOnDay = new HashSet<>();
        for (MenuPlanContent c : allContent) {
            if (c.getDayIdx() == day) {
                contentOnDay.add(c);
            }
        }

        return contentOnDay;
    }

    @Override
    public Set<MenuPlanContentDetailDto> getContentsOfMenuPlanOnDayAsDetailDto(MenuPlan plan, int day) throws NotFoundException, IllegalArgumentException {
        Set<MenuPlanContent> contents = this.getContentsOfMenuPlanOnDay(plan, day);
        return this.convertContentsToDetailDtos(contents);
    }

    @Override
    public MenuPlanContent getContentOfMenuPlanOnDayAndTimeslot(MenuPlan plan, int day, int timeslot) throws NotFoundException, IllegalArgumentException {
        Set<MenuPlanContent> allContent = this.getContentsOfMenuPlanOnDay(plan, day);

        for (MenuPlanContent c : allContent) {
            if (c.getTimeslot() == timeslot) {
                return c;
            }
        }

        return null;
    }

    @Override
    public MenuPlanContentDetailDto getContentOfMenuPlanOnDayAndTimeslotAsDetailDto(MenuPlan plan, int day, int timeslot)
        throws NotFoundException, IllegalArgumentException {
        MenuPlanContent content = this.getContentOfMenuPlanOnDayAndTimeslot(plan, day, timeslot);
        return content != null ? this.convertContentToDetailDto(content) : null;
    }

    @Override
    public MenuPlanContent getContentOfMenuPlanById(MenuPlanContentId contentId) throws NotFoundException, IllegalArgumentException {
        MenuPlan plan = contentId.getMenuplan();
        int day = contentId.getDayIdx();
        int timeslot = contentId.getTimeslot();

        return this.getContentOfMenuPlanOnDayAndTimeslot(plan, day, timeslot);
    }

    @Override
    public MenuPlanContentDetailDto getContentOfMenuPlanByIdAsDetailDto(MenuPlanContentId contentId) throws IllegalArgumentException {
        MenuPlanContent content = this.getContentOfMenuPlanById(contentId);
        return content != null ? this.convertContentToDetailDto(content) : null;
    }

    /* HELPER FUNCTIONS */

    /**
     * Helper function to convert MenuPlanContent objects into their corresponding detail DTO object.
     *
     * @param c the MenuPlanContent object to convert.
     * @return the content's detail DTO.
     * @author Marc Putz
     */
    private MenuPlanContentDetailDto convertContentToDetailDto(MenuPlanContent c) {
        Recipe r = c.getRecipe();
        RecipeListDto recipeListDto = new RecipeListDto("", r.getName(), r.getId(), r.getPictureId());

        return new MenuPlanContentDetailDto()
            .setDay(c.getDayIdx())
            .setTimeslot(c.getTimeslot())
            .setRecipe(recipeListDto);

    }

    /**
     * Helper function to convert MenuPlanContent sets into sets of their corresponding detail DTO objects.
     *
     * @param contents a set of MenuPlanContent objects to convert.
     * @return a set of the content's detail DTOs.
     * @author Marc Putz
     */
    private Set<MenuPlanContentDetailDto> convertContentsToDetailDtos(Set<MenuPlanContent> contents) {
        Set<MenuPlanContentDetailDto> dtos = new HashSet<>();
        for (MenuPlanContent c : contents) {
            dtos.add(this.convertContentToDetailDto(c));
        }
        return dtos;
    }

    /**
     * This only checks if a value is null, if so it replaces it with -1f.
     *
     * @param value which we want to check
     * @return new value which is 100% not null
     */
    private float nullFixer(Float value) {
        if (value == null) {
            return -1f;
        }
        return value;
    }

    /* INVENTORY INGREDIENT FUNCTIONS */

    @Override
    public void createFridge(MenuPlan menuPlan, List<String> fridge) throws ValidationException, ConflictException {
        LOGGER.trace("createFridge({}, {})", menuPlan, fridge);
        this.validator.validateFridge(fridge);

        ArrayList<InventoryIngredient> newInventory = new ArrayList<>();

        for (String ingredientStr : fridge) {
            // check if it's a specific RID-ingredient or basic Ingredient
            List<Ingredient> normalIngredients = this.ingredientService.getByNameMatching(ingredientStr);
            List<RecipeIngredient> recipeIngredients = this.recipeService.findMatchingRecipeIngredients(ingredientStr);

            // basic
            if (!normalIngredients.isEmpty()) {
                Ingredient normalIngredient = normalIngredients.get(0);
                InventoryIngredient newInventoryIngredient =
                    new InventoryIngredient(normalIngredient.getName(), menuPlan.getId(), normalIngredient.getId(),
                        "", -1f, null, true);
                newInventory.add(newInventoryIngredient);
                // detailed
            } else if (!recipeIngredients.isEmpty()) {
                RecipeIngredient recipe = recipeIngredients.get(0);
                InventoryIngredient newInventoryIngredient =
                    new InventoryIngredient(recipe.getAmount().getIngredient(), menuPlan.getId(), recipe.getIngredient().getId(),
                        recipe.getAmount().getIngredient(), -1f, null, true);
                newInventory.add(newInventoryIngredient);
            }
        }

        this.inventoryIngredientRepository.saveAll(newInventory);
    }

    @Override
    public void createInventory(ApplicationUser user) {
        LOGGER.trace("createInventory({})", user);

        // MenuPlan menuPlan = this.getMenuPlanForUserOnDate(user, LocalDate.now());
        MenuPlan menuPlan = null;

        // TODO this is a workaround since getMenuPlanForUserOnDate is not implemented
        List<MenuPlan> menuPlans = this.getAllMenuPlansOfUserDuringTimeframe(user, LocalDate.now().minusDays(5), LocalDate.now());

        if (menuPlans.size() == 1) {
            menuPlan = menuPlans.get(0);
        }

        if (menuPlan != null) {
            HashMap<Long, InventoryIngredient> newNewInventory = new HashMap<>();
            ArrayList<InventoryIngredient> newInventory = new ArrayList<>();

            HashMap<String, InventoryIngredient> detailedInventory = new HashMap<>();
            HashMap<Long, InventoryIngredient> basicInventory = new HashMap<>();

            // getting possible existing fridge
            List<InventoryIngredient> existingFridge = this.inventoryIngredientRepository.findAllyByMenuPlanId(menuPlan.getId());
            if (!existingFridge.isEmpty()) {
                for (InventoryIngredient inv : existingFridge) {
                    // basic ingredients do not have a detailed ingredient Name
                    if (inv.getDetailedIngredientName().trim().isEmpty()) {
                        basicInventory.put(inv.getBasicIngredientId(), inv);
                    } else {
                        detailedInventory.put(inv.getFridgeStringIdentifier(), inv);
                    }

                    this.inventoryIngredientRepository.deleteById(inv.getId());
                }
            }

            for (MenuPlanContent content : menuPlan.getContent()) {
                Recipe recipe = content.getRecipe();

                // for each ingredient add it to the inventory
                for (RecipeIngredient recipeIngredient : recipe.getIngredients()) {
                    /*
                    possible cases:
                    - new ingredient
                    or
                    - basic ingredient matching but not detailed --> marking it as done but not combining
                    or
                    - basic ingredient and detailed matching
                        - already exists but not same unit
                        - already exists but same unit
                     */

                    // basic ingredient exist -> we mark everything as already bought,
                    // but we need to check if we can sum them up
                    boolean basicIngrededientExists = false;
                    if (basicInventory.containsKey(recipeIngredient.getIngredient().getId())) {
                        basicIngrededientExists = true;
                    }

                    // detailed does not exist
                    if (!detailedInventory.containsKey(recipeIngredient.getAmount().getFridgeStringIdentifier())) {
                        detailedInventory.put(recipeIngredient.getAmount().getFridgeStringIdentifier(),
                            new InventoryIngredient(recipeIngredient.getAmount().getIngredient(), menuPlan.getId(), recipeIngredient.getIngredient().getId(),
                                recipeIngredient.getAmount().getIngredient(), nullFixer(recipeIngredient.getAmount().getAmount()), recipeIngredient.getAmount()
                                .getUnit(), basicIngrededientExists));
                    } else {
                        InventoryIngredient existingIngredient = detailedInventory.get(recipeIngredient.getAmount().getFridgeStringIdentifier());
                        // detailed same unit and ingredient --> combining them
                        if (detailedInventory.containsKey(recipeIngredient.getAmount().getFridgeStringIdentifier())) {
                            float combinedAmount = existingIngredient.getAmount();
                            float newAmount = nullFixer(recipeIngredient.getAmount().getAmount());
                            if (newAmount > 0) {
                                combinedAmount = combinedAmount != -1f ? newAmount + combinedAmount : newAmount;
                            }

                            InventoryIngredient combinedInvIngredient = new InventoryIngredient(
                                existingIngredient.getName(), existingIngredient.getMenuPlanId(), existingIngredient.getBasicIngredientId(),
                                existingIngredient.getDetailedIngredientName(), combinedAmount, existingIngredient.getUnit(),
                                basicIngrededientExists ? basicIngrededientExists : existingIngredient.getInventoryStatus());

                            detailedInventory.put(existingIngredient.getFridgeStringIdentifier(), combinedInvIngredient);
                            // if detailed name matches, existing unit is null and available (= was put into the fridge)
                            // we do not combine but mark as available
                        } else if (existingIngredient.getDetailedIngredientName().equals(recipeIngredient.getAmount().getIngredient())
                            && existingIngredient.getUnit() == null && existingIngredient.getInventoryStatus()) {
                            detailedInventory.put(recipeIngredient.getAmount().getFridgeStringIdentifier(),
                                new InventoryIngredient(recipeIngredient.getAmount().getIngredient(), menuPlan.getId(),
                                    recipeIngredient.getIngredient().getId(),
                                    recipeIngredient.getAmount().getIngredient(), nullFixer(recipeIngredient.getAmount().getAmount()),
                                    recipeIngredient.getAmount()
                                        .getUnit(), true));
                        }
                    }
                }
            }
            this.inventoryIngredientRepository.saveAll(basicInventory.values());
            this.inventoryIngredientRepository.saveAll(detailedInventory.values());
            LOGGER.debug("Creating inventory was successful");
        }
    }

    @Override
    public InventoryListDto searchInventory(ApplicationUser user, boolean onlyValid) {
        LOGGER.trace("searchInventory({}, {})", user, onlyValid);
        if (onlyValid) {
            // MenuPlan menuPlan = this.menuPlanService.getMenuPlanForUserOnDate(user, LocalDate.now());

            // TODO this is a workaround since getMenuPlanForUserOnDate is not implemented
            MenuPlan menuPlan = null;
            List<MenuPlan> menuPlans = this.getAllMenuPlansOfUserDuringTimeframe(user, LocalDate.now().minusDays(5), LocalDate.now());

            if (menuPlans.size() == 1) {
                menuPlan = menuPlans.get(0);

                return this.searchInventory(menuPlan.getId());
            } else {
                return null;
            }
        } else {
            // not sure if needed
            throw new NotImplementedException();
        }
    }

    @Override
    public InventoryListDto searchInventory(Long menuPlanId) {
        LOGGER.trace("searchInventory({})", menuPlanId);
        List<InventoryIngredientDto> missing = new ArrayList<>();
        List<InventoryIngredientDto> available = new ArrayList<>();
        if (menuPlanId != null) {
            List<InventoryIngredient> inventory = this.inventoryIngredientRepository.findAllyByMenuPlanId(menuPlanId);

            for (InventoryIngredient ingred : inventory) {
                InventoryIngredientDto newDto =
                    new InventoryIngredientDto(ingred.getId(), ingred.getName(), ingred.getMenuPlanId(), ingred.getBasicIngredientId(),
                        ingred.getDetailedIngredientName(),
                        ingred.getAmount() == null ? -1f : ingred.getAmount(),
                        ingred.getUnit(), ingred.getInventoryStatus());

                if (ingred.getInventoryStatus()) {
                    available.add(newDto);
                } else {
                    missing.add(newDto);
                }
            }
        }
        return new InventoryListDto(missing, available);
    }

    @Override
    public void updateInventoryIngredient(ApplicationUser user, InventoryIngredientDto updatedIngredientDto)
        throws NotFoundException, ConflictException, ValidationException {
        LOGGER.trace("updateInventoryIngredient({}, {})", user, updatedIngredientDto);
        this.validator.validateInventoryIngredientForUpdate(updatedIngredientDto, searchInventory(updatedIngredientDto.getMenuPlanId()));

        InventoryIngredient toSave = this.inventoryIngredientRepository.findById(updatedIngredientDto.getId()).get();
        toSave.setInventoryStatus(updatedIngredientDto.isInventoryStatus());
        this.inventoryIngredientRepository.save(toSave);
    }

    @Override
    public void updateInventoryIngredient(InventoryIngredientDto updatedIngredientDto) throws NotFoundException, ConflictException {
        throw new NotImplementedException();
    }
}
