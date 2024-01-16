package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan.MenuPlanCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan.MenuPlanDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.MenuPlan;
import at.ac.tuwien.sepr.groupphase.backend.entity.MenuPlanContent;
import at.ac.tuwien.sepr.groupphase.backend.entity.Profile;
import at.ac.tuwien.sepr.groupphase.backend.entity.idclasses.MenuPlanContentId;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.DataStoreException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.MenuPlanRepository;
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
import java.security.InvalidParameterException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public List<MenuPlan> getAllMenuPlansOfUser(ApplicationUser user) {
        throw new NotImplementedException();
    }

    @Override
    public List<MenuPlan> getAllMenuPlansOfUserDuringTimeframe(ApplicationUser user, LocalDate from, LocalDate until) {
        return menuPlanRepository.getAllByUserMatchingTimeframe(user, from, until);
    }

    @Override
    public MenuPlan getMenuPlanForUserOnDate(ApplicationUser user, LocalDate date) {
        throw new NotImplementedException();
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

        // TODO: get list of recipes
        // TODO: filter according to profile
        List<RecipeListDto> recipes = new ArrayList<>();

        // TODO: generate menu plan content
        Set<MenuPlanContent> contents = new HashSet<>();

        // create menu plan entity
        MenuPlan menuPlan = new MenuPlan()
            .setFromDate(from)
            .setUntilDate(until)
            .setUser(user)
            .setProfile(profile)
            .setContent(contents);


        try {
            // save menu plan to data store
            menuPlanRepository.save(menuPlan);

            // TODO: save contents to data store (if needed? see jpa documentation)

            // TODO: retrieve final objects from data store and return detail dto
            return new MenuPlanDetailDto()
                .setUserId(menuPlan.getUser().getId())
                // TODO: add profile information to detail dto
                .setProfileId(-1L)
                .setProfileName("Not available")
                //.setProfileId(menuPlan.getProfile().getId())
                //.setProfileName(menuPlan.getProfile().getName())
                .setUntilTime(menuPlan.getUntilDate())
                .setFromTime(menuPlan.getFromDate())
                .setNumDays((int) Duration.between(
                    menuPlan.getFromDate().atStartOfDay(),
                    menuPlan.getUntilDate().atStartOfDay()
                ).toDays() + 1);
        } catch (JDBCException e) {
            throw new DataStoreException(e.getErrorMessage(), e);
        }
    }

    @Override
    public void deleteMenuPlan(MenuPlan toDelete) throws DataStoreException {
        throw new NotImplementedException();
    }

    @Override
    public MenuPlan updateMenuPlan(MenuPlan toUpdate) throws DataStoreException, ValidationException, ConflictException {
        throw new NotImplementedException();
    }

    @Override
    public Set<MenuPlanContent> getContentsOfMenuPlan(MenuPlan plan) {
        MenuPlan menuPlan = menuPlanRepository.getReferenceById(plan.getId());
        return menuPlan.getContent();
    }

    @Override
    public List<MenuPlanContent> getContentsOfMenuPlanOnDay(MenuPlan plan, int day) throws InvalidParameterException {
        throw new NotImplementedException();
    }

    @Override
    public MenuPlanContent getContentOfMenuPlanOnDayAndTimeslot(MenuPlan plan, int day, int timeslot) throws InvalidParameterException {
        throw new NotImplementedException();
    }

    @Override
    public MenuPlanContent getContentOfMenuPlanById(MenuPlanContentId contentId) throws InvalidParameterException {
        throw new NotImplementedException();
    }

    @Override
    public MenuPlanContent updateMenuPlanContent(MenuPlanContent content) throws DataStoreException, ValidationException {
        throw new NotImplementedException();
    }
}
