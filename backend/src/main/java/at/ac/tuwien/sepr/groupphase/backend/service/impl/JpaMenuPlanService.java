package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InventoryIngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InventoryListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan.MenuPlanContentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan.MenuPlanDetailDto;
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
import at.ac.tuwien.sepr.groupphase.backend.repository.IngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.InventoryIngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.MenuPlanRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RecipeIngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.MenuPlanService;
import at.ac.tuwien.sepr.groupphase.backend.service.RecipeService;
import at.ac.tuwien.sepr.groupphase.backend.service.validation.MenuPlanValidator;
import org.apache.commons.lang3.NotImplementedException;
import org.hibernate.JDBCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    private final IngredientRepository ingredientRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final MenuPlanRepository menuPlanRepository;
    private final InventoryIngredientRepository inventoryIngredientRepository;

    @Autowired
    public JpaMenuPlanService(MenuPlanRepository repository, RecipeService recipeService, MenuPlanValidator validator,
                              IngredientRepository ingredientRepository,
                              RecipeIngredientRepository recipeIngredientRepository, InventoryIngredientRepository inventoryIngredientRepository) {
        this.validator = validator;
        this.menuPlanRepository = repository;
        this.recipeService = recipeService;
        this.ingredientRepository = ingredientRepository;
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

    public List<MenuPlanDetailDto> getAllMenuPlansofUserDetailDto(ApplicationUser user) {
        List<MenuPlan> plans = menuPlanRepository.getAllByUser(user);
        List<MenuPlanDetailDto> details = new ArrayList<>();
        for (MenuPlan plan : plans) {
            MenuPlanDetailDto detail = new MenuPlanDetailDto();
            //Set<MenuPlanContentDetailDto> contents = getContentsOfMenuPlanAsDetailDto(plan);
            detail.setUserId(user.getId()).setFromTime(plan.getFromDate());  // .setContents(contents)
            detail.setUntilTime(plan.getUntilDate()).setNumDays(7);
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
        menuPlanDetailDto.setUntilTime(menuPlan.getUntilDate());
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

        // make sure plan parameter is not NULL
        if (plan == null) {
            throw new IllegalArgumentException("MenuPlan parameter cannot be NULL value");
        }

        // get fridge contents
        List<Long> fridgeIngredientsIds = new ArrayList<>(); // TODO: insert command here
        List<Ingredient> fridgeIngredients = new ArrayList<>();
        for (Long ingredientId : fridgeIngredientsIds) {
            // TODO: get ingredient object by ID and add to fridgeIngredients list
        }

        // TODO: get allergenes / allergic ingredients

        // TODO: get profile ingredient preferences

        // define content sets
        Set<MenuPlanContent> contents = new HashSet<>();
        Set<MenuPlanContentDetailDto> contentDtos = new HashSet<>();

        // get available recipes from data store
        List<Recipe> availableRecipes = new ArrayList<>(); // TODO: replace

        // TODO: remove unsuited recipes from list

        // select recipes from list and populate content lists
        int numDays = (int) Duration.between(plan.getFromDate().atStartOfDay(), plan.getUntilDate().atStartOfDay()).toDays() + 1;
        final int timeslotsPerDay = 3; // TODO: make this changeable
        if (!availableRecipes.isEmpty()) {

            // set to keep track of already added recipes, so nothing is added twice
            Set<Long> usedIds = new HashSet<>();

            for (int day = 0; day < numDays; day++) {
                for (int timeslot = 0; timeslot < timeslotsPerDay; timeslot++) {

                    // get random recipe from list
                    Recipe r = null;
                    final int maxRetries = 30;
                    int retry = 0;
                    while (r == null && retry < maxRetries) {
                        // generate random index and get from list
                        int recipeIdx = ThreadLocalRandom.current().nextInt(0, availableRecipes.size());
                        r = availableRecipes.get(recipeIdx);

                        // make sure no ID is used twice
                        if (!usedIds.contains(r.getId())) {

                            // TODO: further checks if recipe can be used, if not set r back to NULL and mark as used

                        } else {
                            // recipe already used, reset to NULL
                            r = null;
                        }
                        retry++;
                    }

                    // check if a recipe was found
                    if (r == null) {
                        throw new ConflictException("Cannot generate menu plan", List.of("Menu Plan can not find enough content in data store"));
                    }

                    usedIds.add(r.getId());

                    // create content entity and add to list
                    MenuPlanContent content = new MenuPlanContent()
                        .setRecipe(r)
                        .setTimeslot(timeslot)
                        .setDayIdx(day);
                    contents.add(content);

                    // create content detail dto
                    MenuPlanContentDetailDto contentDto = new MenuPlanContentDetailDto()
                        .setDay(day)
                        .setTimeslot(timeslot)
                        .setRecipe(new RecipeListDto(
                            "",
                            r.getName(),
                            r.getId(),
                            r.getPicture()
                        ));
                    contentDtos.add(contentDto);

                }

            }
        }

        // add contents to menu plan
        for (MenuPlanContent c : contents) {
            plan.addContent(c);
        }

        try {

            // save menu plan
            MenuPlan savedPlan = this.menuPlanRepository.save(plan);

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

        return this.menuPlanRepository.save(menuPlan);
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
        RecipeListDto recipeListDto = new RecipeListDto("", r.getName(), r.getId(), r.getPicture());

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

    /* INVENTORY INGREDIENT FUNCTIONS */

    @Override
    public void createFridge(MenuPlan menuPlan, List<String> fridge) throws ValidationException, ConflictException {
        this.validator.validateFridge(fridge);

        ArrayList<InventoryIngredient> newInventory = new ArrayList<>();

        for (String ingredientStr : fridge) {
            var result = this.ingredientRepository.findByNameContainingIgnoreCase(ingredientStr);

            if (!result.isEmpty()) {
                Ingredient ingredient = result.get(0);

                List<RecipeIngredient> recipeIngredients = this.recipeIngredientRepository.findByIngredient_Id(ingredient.getId());

                if (!recipeIngredients.isEmpty()) {
                    InventoryIngredient newInventoryIngredient = new InventoryIngredient(ingredient.getName(), menuPlan.getId(), recipeIngredients.get(0).getId(),
                        null, null, true);
                    newInventory.add(newInventoryIngredient);
                }
            }
        }

        this.inventoryIngredientRepository.saveAll(newInventory);
    }

    @Override
    public void createInventory(ApplicationUser user) {
        // MenuPlan menuPlan = this.menuPlanService.getMenuPlanForUserOnDate(user, LocalDate.now());
        MenuPlan menuPlan = null;

        // TODO this is a workaround since getMenuPlanForUserOnDate is not implemented
        List<MenuPlan> menuPlans = this.getAllMenuPlansOfUserDuringTimeframe(user, LocalDate.now().minusDays(5), LocalDate.now());

        if (menuPlans.size() == 1) {
            menuPlan = menuPlans.get(0);
        }

        if (menuPlan != null) {
            HashMap<Long, InventoryIngredient> newInventory = new HashMap<>();
            HashMap<String, InventoryIngredient> newNewInventory = new HashMap<>();

            for (MenuPlanContent content : menuPlan.getContent()) {
                Recipe recipe = content.getRecipe();

                // for each ingredient add it to the inventory
                for (RecipeIngredient recipeIngredient : recipe.getIngredients()) {
                    // if it does not exist we just add it
                    if (!newNewInventory.containsKey(recipeIngredient.getIngredient().getName())) {
                        newNewInventory.put(recipeIngredient.getIngredient().getName(), new InventoryIngredient(
                            recipeIngredient.getIngredient().getName(), menuPlan.getId(), recipeIngredient.getId(),
                            recipeIngredient.getAmount().getAmount(),
                            recipeIngredient.getAmount().getUnit(), false
                        ));
                    } else if (newNewInventory.get(recipeIngredient.getIngredient().getName()).getUnit() == recipeIngredient.getAmount().getUnit()) {
                        InventoryIngredient existingEntry = newNewInventory.get(recipeIngredient.getIngredient().getName());

                        Float newAmount = existingEntry.getAmount();
                        // combining them if none of them are null
                        if (recipeIngredient.getAmount().getAmount() != null && newAmount != null) {
                            newAmount += recipeIngredient.getAmount().getAmount();
                        }

                        InventoryIngredient newEntry =
                            new InventoryIngredient(existingEntry.getName(), menuPlan.getId(), existingEntry.getIngredientId(), newAmount,
                                existingEntry.getUnit(), false);

                        newNewInventory.put(recipeIngredient.getIngredient().getName(), newEntry);
                    } else {
                        // different unit, not adding them up together
                        newNewInventory.put(recipeIngredient.getIngredient().getName(), new InventoryIngredient(
                            recipeIngredient.getIngredient().getName(), menuPlan.getId(), recipeIngredient.getId(),
                            recipeIngredient.getAmount().getAmount() != null ? recipeIngredient.getAmount().getAmount() : 0.0f,
                            recipeIngredient.getAmount().getUnit(), false));
                    }
                }
            }
            this.inventoryIngredientRepository.saveAll(newNewInventory.values());
        }
    }

    @Override
    public InventoryListDto searchInventory(ApplicationUser user, boolean onlyValid) {
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
        List<InventoryIngredientDto> missing = new ArrayList<>();
        List<InventoryIngredientDto> available = new ArrayList<>();
        if (menuPlanId != null) {
            List<InventoryIngredient> inventory = this.inventoryIngredientRepository.findAllyByMenuPlanId(menuPlanId);

            for (InventoryIngredient ingred : inventory) {
                InventoryIngredientDto newDto =
                    new InventoryIngredientDto(ingred.getId(), ingred.getName(), ingred.getMenuPlanId(), ingred.getIngredientId(), ingred.getAmount(),
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
    public void updateInventoryIngredient(ApplicationUser user, InventoryIngredientDto updatedIngredientDto) throws NotFoundException, ConflictException {
        // TODO Validation
        InventoryIngredient toSave = this.inventoryIngredientRepository.findById(updatedIngredientDto.getId()).get();
        toSave.setInventoryStatus(updatedIngredientDto.isInventoryStatus());
        this.inventoryIngredientRepository.save(toSave);
    }

    @Override
    public void updateInventoryIngredient(InventoryIngredientDto updatedIngredientDto) throws NotFoundException, ConflictException {
        throw new NotImplementedException();
    }
}
