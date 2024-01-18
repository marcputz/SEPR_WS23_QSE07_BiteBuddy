package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan.MenuPlanContentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan.MenuPlanCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan.MenuPlanDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.MenuPlan;
import at.ac.tuwien.sepr.groupphase.backend.entity.MenuPlanContent;
import at.ac.tuwien.sepr.groupphase.backend.entity.Profile;
import at.ac.tuwien.sepr.groupphase.backend.entity.idclasses.MenuPlanContentId;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.DataStoreException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Service class definition for MenuPlan entities.
 *
 * @author Marc Putz
 */
public interface MenuPlanService {

    /**
     * Gets a menu plan entity by its ID.
     *
     * @param id the ID of the menu plan to get.
     * @return the menu plan entity
     * @throws NotFoundException if the entity couldn't be found in the data store.
     * @author Marc Putz
     */
    MenuPlan getById(long id) throws NotFoundException;

    /**
     * Gets a list of all MenuPlan entities associated with a User.
     *
     * @param user the user for which to get all menu plans for.
     * @return a list of menu plans, can be empty if no menu plans are found.
     * @author Marc Putz
     */
    List<MenuPlan> getAllMenuPlansOfUser(ApplicationUser user);

    /**
     * Gets a list of all MenuPlan entities associated with a user during a specified timeframe.
     *
     * @param user the user for which to get menu plans for.
     * @param from the start date to filter menu plans. All menu plans ending on or after this date will be included.
     * @param until the end date to filter menu plans. All menu plans starting on or before this date will be included.
     * @return a list of menu plans, can be empty if no menu plans match the criteria.
     * @author Marc Putz
     */
    List<MenuPlan> getAllMenuPlansOfUserDuringTimeframe(ApplicationUser user, LocalDate from, LocalDate until);

    /**
     * Gets the MenuPlan entity associated with a user which is valid on the specified date.
     *
     * @param user the user for which to get the menu plan for.
     * @param date the date for which the menu plan is valid for.
     * @return the menu plan matching these criteria or NULL if no menu plan matching the criteria is found.
     * @author Marc Putz
     */
    MenuPlan getMenuPlanForUserOnDate(ApplicationUser user, LocalDate date);

    /**
     * Generates a new menu plan for a user using the specified profile and timeframe and saves it to the data store.
     * Only one menu plan can be valid for a specific timeframe, otherwise a {@link ConflictException} may be thrown.
     *
     * @param user the user for which to create a new menu plan for.
     * @param profile the profile to use for generating the menu plan.
     * @param from the start time for this menu plan to be valid (inclusive).
     * @param until the end time for this menu plan to be valid (inclusive).
     * @return the generated menu plan as a detail dto
     * @throws DataStoreException if the data store is unable to process the new entity.
     * @throws ConflictException if the new entity is in conflict with the current state of the data store (e.g. another menu plan already exists during the specified timeframe).
     * @throws ValidationException if the parameters provided are invalid and cannot be used for menu plan generation.
     * @author Marc Putz
     */
    MenuPlanDetailDto generateMenuPlan(ApplicationUser user, Profile profile, LocalDate from, LocalDate until) throws DataStoreException, ConflictException, ValidationException;

    /**
     * Generates a new menu plan for a user using the data specified in the MenuPlanCreateDto and saves it to the data store.
     * Only one menu plan can be valid for a specific timefame, otherwise a {@link ConflictException} may be thrown.
     *
     * @param user the user for which to create a new menu plan for.
     * @param createDto the creation dto containing menu plan creation data.
     * @return the generated menu plan as a detail dto.
     * @throws DataStoreException if the data store is unable to process the new entity.
     * @throws ConflictException if the new entity is in conflict with the current state of the data store.
     * @throws ValidationException if the parameters provided are invalid and cannot be used for menu plan generation.
     * @author Marc Putz
     */
    MenuPlanDetailDto generateMenuPlan(ApplicationUser user, MenuPlanCreateDto createDto) throws DataStoreException, ConflictException, ValidationException;

    /**
     * Deletes a menu plan from the data store. If the entity doesn't exist, this method does nothing.
     *
     * @param toDelete the menu plan entity to delete.
     * @throws DataStoreException if the data store is unable to process the request.
     * @author Marc Putz
     */
    void deleteMenuPlan(MenuPlan toDelete) throws DataStoreException;

    /**
     * Deletes a menu plan from the data store. If the entity doesn't exist, this method does nothing.
     *
     * @param id the ID of the menu plan to delete
     * @throws DataStoreException if the data store is unable to process the request.
     * @author Marc Putz
     */
    void deleteMenuPlan(long id) throws DataStoreException;

    /**
     * Updates a menu plan in the data store. Uses the menu plan's ID to identify the entity.
     *
     * @param toUpdate the menu plan to update.
     * @return the updated menu plan entity.
     * @throws DataStoreException if the data store is unable to process the request.
     * @throws ValidationException if the data in the entity is invalid
     * @throws ConflictException if the updated entity is in conflict with the current state of the data store (e.g. there's another menu plan active during the same timeframe)
     * @author Marc Putz
     */
    MenuPlanDetailDto updateMenuPlan(MenuPlan toUpdate) throws DataStoreException, ValidationException, ConflictException;

    /**
     * Gets a list of all recipes contained in a menu plan.
     *
     * @param plan the menu plan to get contents of.
     * @return a set of menu plan contents. Can be empty if menu plan has no content.
     * @throws NotFoundException if the menu plan specified in the argument list does not exist in the data store.
     * @author Marc Putz
     */
    Set<MenuPlanContent> getContentsOfMenuPlan(MenuPlan plan) throws NotFoundException;

    /**
     * Gets a list of all recipes contained in a menu plan.
     *
     * @param menuPlanId the ID of the menu plan to get contents of.
     * @return a set of menu plan contents. Can be empty if menu plan has no content.
     * @throws NotFoundException if the menu plan specified in the argument list does not exist in the data store.
     * @author Marc Putz
     */
    Set<MenuPlanContent> getContentsOfMenuPlan(long menuPlanId) throws NotFoundException;

    /**
     * Gets a list of all recipes contained in a menu plan as it's corresponding detail DTO.
     *
     * @param plan the menu plan to get contents of.
     * @return a set of menu plan contents as detail DTOs. Can be empty if menu plan has no content.
     * @throws NotFoundException if the menu plan specified in the argument list does not exist in the data store.
     * @author Marc Putz
     */
    Set<MenuPlanContentDetailDto> getContentsOfMenuPlanAsDetailDto(MenuPlan plan) throws NotFoundException;

    /**
     * Gets a list of all recipes contained in a menu plan as it's corresponding detail DTO.
     *
     * @param menuPlanId the ID of the menu plan to get contents of.
     * @return a set of menu plan contents as detail DTOs. Can be empty if menu plan has no content.
     * @throws NotFoundException if the menu plan specified in the argument list does not exist in the data store.
     * @author Marc Putz
     */
    Set<MenuPlanContentDetailDto> getContentsOfMenuPlanAsDetailDto(long menuPlanId) throws NotFoundException;

    /**
     * Gets a list of all recipes in a menu plan on a specified day.
     *
     * @param plan the menu plan to get contents of.
     * @param day the day for which to get contents of. (0 = first day, 1 = second day, etc.)
     * @return a set of menu plan contents on the specified day. Can be empty if menu plan has no contents on that day.
     * @throws NotFoundException if the menu plan specified in the argument list could not be found in the data store.
     * @throws IllegalArgumentException if the specified day is not within range of the menu plan (e.g. menu plan has 7 days, day = 10 would be invalid)
     * @author Marc Putz
     */
    Set<MenuPlanContent> getContentsOfMenuPlanOnDay(MenuPlan plan, int day) throws NotFoundException, IllegalArgumentException;

    /**
     * Gets a list of all recipes in a menu plan on a specified day as their corresponding detail DTOs.
     *
     * @param plan the menu plan to get contents of.
     * @param day the day for which to get contents of. (0 = first day, 1 = second day, etc.)
     * @return a set of menu plan content detail DTOs on the specified day. Can be empty if menu plan has no contents on that day.
     * @throws NotFoundException if the menu plan specified in the argument list could not be found in the data store.
     * @throws IllegalArgumentException if the specified day is not within range of the menu plan (e.g. menu plan has 7 days, day = 10 would be invalid)
     * @author Marc Putz
     */
    Set<MenuPlanContentDetailDto> getContentsOfMenuPlanOnDayAsDetailDto(MenuPlan plan, int day) throws NotFoundException, IllegalArgumentException;

    /**
     * Gets the content of a menu plan by day and timeslot.
     *
     * @param plan the menu plan to get the content of.
     * @param day the day for which to get content of. (0 = first day, 1 = second day, etc.)
     * @param timeslot the timeslot of the day to get content of (0 = first slot, 1 = second slot, etc.)
     * @return the menu plan content specified on the given day and timeslot. Can be NULL if no content is specified.
     * @throws NotFoundException if the menu plan specified in the argument list could not be found in the data store.
     * @throws IllegalArgumentException if the specified day is not within range of the menu plan (e.g. menu plan has 7 days, day = 10 would be invalid)
     * @author Marc Putz
     */
    MenuPlanContent getContentOfMenuPlanOnDayAndTimeslot(MenuPlan plan, int day, int timeslot) throws NotFoundException, IllegalArgumentException;

    /**
     * Gets the content of a menu plan by day and timeslot as its corresponding detail DTO.
     *
     * @param plan the menu plan to get the content of.
     * @param day the day for which to get content of. (0 = first day, 1 = second day, etc.)
     * @param timeslot the timeslot of the day to get content of (0 = first slot, 1 = second slot, etc.)
     * @return the menu plan content detail DTO specified on the given day and timeslot. Can be NULL if no content is specified.
     * @throws NotFoundException if the menu plan specified in the argument list could not be found in the data store.
     * @throws IllegalArgumentException if the specified day is not within range of the menu plan (e.g. menu plan has 7 days, day = 10 would be invalid)
     * @author Marc Putz
     */
    MenuPlanContentDetailDto getContentOfMenuPlanOnDayAndTimeslotAsDetailDto(MenuPlan plan, int day, int timeslot) throws NotFoundException, IllegalArgumentException;

    /**
     * Gets the content of a menu plan by its ID class.
     *
     * @param contentId the ID class of the menu plan content containing the search parameters.
     * @return the menu plan content specified by the ID object. Can be NULL if no content is specified.
     * @throws NotFoundException if the menu plan specified in the ID object could not be found in the data store.
     * @throws IllegalArgumentException if parameters in ID object are not within range of the menu plan (e.g. too many days, invalid timeslot, etc.)
     * @author Marc Putz
     */
    MenuPlanContent getContentOfMenuPlanById(MenuPlanContentId contentId) throws NotFoundException, IllegalArgumentException;

    /**
     * Gets the content of a menu plan by its ID class as its corresponding detail DTO.
     *
     * @param contentId the ID class of the menu plan content containing the search parameters.
     * @return the menu plan content detail DTO specified by the ID object. Can be NULL if no content is specified.
     * @throws NotFoundException if the menu plan specified in the ID object could not be found in the data store.
     * @throws IllegalArgumentException if parameters in ID object are not within range of the menu plan (e.g. too many days, invalid timeslot, etc.)
     * @author Marc Putz
     */
    MenuPlanContentDetailDto getContentOfMenuPlanByIdAsDetailDto(MenuPlanContentId contentId) throws NotFoundException, IllegalArgumentException;
}
