package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan.MenuPlanContentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan.MenuPlanCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan.MenuPlanDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.MenuPlan;
import at.ac.tuwien.sepr.groupphase.backend.entity.MenuPlanContent;
import at.ac.tuwien.sepr.groupphase.backend.entity.Profile;
import at.ac.tuwien.sepr.groupphase.backend.entity.Recipe;
import at.ac.tuwien.sepr.groupphase.backend.entity.idclasses.MenuPlanContentId;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.DataStoreException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.MenuPlanRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.MenuPlanService;
import at.ac.tuwien.sepr.groupphase.backend.service.RecipeService;
import at.ac.tuwien.sepr.groupphase.backend.service.validation.MenuPlanValidator;
import org.hibernate.JDBCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.time.LocalDate;
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
    private final MenuPlanRepository menuPlanRepository;

    @Autowired
    public JpaMenuPlanService(MenuPlanRepository repository, RecipeService recipeService, MenuPlanValidator validator) {
        this.validator = validator;
        this.menuPlanRepository = repository;
        this.recipeService = recipeService;
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
    public List<MenuPlan> getAllMenuPlansOfUserDuringTimeframe(ApplicationUser user, LocalDate from, LocalDate until) {
        return menuPlanRepository.getAllByUserMatchingTimeframe(user, from, until);
    }

    @Override
    public MenuPlan getMenuPlanForUserOnDate(ApplicationUser user, LocalDate date) {
        return menuPlanRepository.getByUserOnDate(user, date);
    }

    @Override
    public MenuPlanDetailDto generateMenuPlan(ApplicationUser user, MenuPlanCreateDto createDto) throws DataStoreException, ConflictException, ValidationException {
        LocalDate fromDate = createDto.getFromTime();
        LocalDate untilDate = createDto.getUntilTime();
        // TODO: set profile defined in dto class
        Profile profile = null;
        return generateMenuPlan(user, profile, fromDate, untilDate);
    }

    @Override
    public MenuPlanDetailDto generateMenuPlan(ApplicationUser user, Profile profile, LocalDate from, LocalDate until) throws DataStoreException, ConflictException, ValidationException {
        LOGGER.trace("generateMenuPlan({},{},{},{})", user, profile, from, until);

        // validate inputs
        validator.validateForCreate(user, profile, from, until);

        // check if there are already existing menu plans in the specified timeframe
        List<MenuPlan> conflictingMenuPlans = this.getAllMenuPlansOfUserDuringTimeframe(user, from, until);
        if (!conflictingMenuPlans.isEmpty()) {
            throw new ConflictException("New Menu Plan would conflict with the current system state", List.of("There is already a menu plan active during the specified timeframe"));
        }

        // select recipes
        // TODO: filter according to profile
        Set<MenuPlanContent> contents = new HashSet<>();
        Set<MenuPlanContentDetailDto> contentDtos = new HashSet<>();

        Set<Long> usedIds = new HashSet<>();

        long highestId = recipeService.getHighestRecipeId();
        long lowestId = recipeService.getLowestRecipeId();

        int numDays = (int) Duration.between(from.atStartOfDay(), until.atStartOfDay()).toDays() + 1;
        final int timeslotsPerDay = 3; // TODO: make this changeable
        for (int day = 0; day < numDays; day++) {
            for (int timeslot = 0; timeslot < timeslotsPerDay; timeslot++) {
                // generate random ID
                long recipeId = ThreadLocalRandom.current().nextInt((int) lowestId, (int) highestId + 1);

                // make sure no ID is used twice
                while (usedIds.contains(recipeId)) {
                    recipeId++;
                }

                // find recipe in data store. if not available, try next id (stop after MAX_RETRIES tries)
                Recipe recipe = null;
                final int maxRetries = 30;
                int retry = 0;
                while (recipe == null && retry < maxRetries) {
                    try {
                        recipe = recipeService.getRecipeById(recipeId);
                    } catch (NotFoundException e) {
                        recipeId++;
                        if (recipeId > highestId) {
                            recipeId = lowestId;
                        }
                        retry++;
                    }
                }

                // if no recipe after MAX_RETRIES could be found, throw error
                if (recipe == null) {
                    throw new ConflictException("Cannot generate menu plan", List.of("Menu Plan can not find enough content in data store"));
                }

                // add ID to set of used IDs, so it's not used again
                usedIds.add(recipeId);

                // create content entity and add to list
                MenuPlanContent content = new MenuPlanContent()
                    .setRecipe(recipe)
                    .setTimeslot(timeslot)
                    .setDayIdx(day);
                contents.add(content);

                // create content detail dto
                MenuPlanContentDetailDto contentDto = new MenuPlanContentDetailDto()
                    .setDay(day)
                    .setTimeslot(timeslot)
                    .setRecipe(new RecipeListDto(
                        "",
                        recipe.getName(),
                        recipe.getId(),
                        recipe.getPicture()
                    ));
                contentDtos.add(contentDto);
            }
        }

        // create menu plan entity
        MenuPlan menuPlan = new MenuPlan()
            .setFromDate(from)
            .setUntilDate(until)
            .setUser(user)
            .setProfile(profile);

        // set relationships
        for (MenuPlanContent c : contents) {
            menuPlan.addContent(c);
        }

        try {
            // save menu plan to data store
            MenuPlan savedPlan = menuPlanRepository.save(menuPlan);

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
            throw new ConflictException("New Menu Plan would conflict with the current system state", List.of("There is already a menu plan active during the specified timeframe"));
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
    public MenuPlanContentDetailDto getContentOfMenuPlanOnDayAndTimeslotAsDetailDto(MenuPlan plan, int day, int timeslot) throws NotFoundException, IllegalArgumentException {
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
}
