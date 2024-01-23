package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InventoryIngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InventoryListDto;
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
     * @param user  the user for which to get menu plans for.
     * @param from  the start date to filter menu plans. All menu plans ending on or after this date will be included.
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
     * Generates contents for a menu plan and stores them in the data store.
     * If there is already content stored in the menu plan, it will be erased and replaced with new content.
     *
     * @param plan the menu plan to fill with content
     * @return the menu plan with generated contents as a detail DTO object.
     * @throws DataStoreException if the data store is unable to process the new entity.
     * @throws ConflictException if the current state of the data store does not allow generating content (e.g. not enough recipes available).
     */
    MenuPlanDetailDto generateContent(MenuPlan plan) throws DataStoreException, ConflictException;

    /**
     * Creates a new menu plan entity in the data store.
     * The created menu plan will have no contents yet, these can be generated with the {@code generateContent} function.
     * Only one menu plan can be created for a specific timeframe, otherwise a {@link ConflictException} will be thrown.
     *
     * @param user the user for which to create a new menu plan for.
     * @param profile the profile to use for creating the menu plan.
     * @param from the start time for this menu plan to be valid (inclusive).
     * @param until the end time for this menu plan to be valid (inclusive).
     * @return the created (empty) menu plan entity.
     * @throws DataStoreException if the data store is unable to process the new entity.
     * @throws ConflictException if the new entity is in conflict with the current state of the data store (e.g. another menu plan is already defined during the specified timeframe).
     * @throws ValidationException if the parameters provided are invalid and cannot be used for menu plan creation.
     */
    MenuPlan createEmptyMenuPlan(ApplicationUser user, Profile profile, LocalDate from, LocalDate until)
        throws DataStoreException, ConflictException, ValidationException;

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
     * @throws DataStoreException  if the data store is unable to process the request.
     * @throws ValidationException if the data in the entity is invalid
     * @throws ConflictException   if the updated entity is in conflict with the current state of the data store (e.g. there's another menu plan active during the same timeframe)
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
     * @param day  the day for which to get contents of. (0 = first day, 1 = second day, etc.)
     * @return a set of menu plan contents on the specified day. Can be empty if menu plan has no contents on that day.
     * @throws NotFoundException        if the menu plan specified in the argument list could not be found in the data store.
     * @throws IllegalArgumentException if the specified day is not within range of the menu plan (e.g. menu plan has 7 days, day = 10 would be invalid)
     * @author Marc Putz
     */
    Set<MenuPlanContent> getContentsOfMenuPlanOnDay(MenuPlan plan, int day) throws NotFoundException, IllegalArgumentException;

    /**
     * Gets a list of all recipes in a menu plan on a specified day as their corresponding detail DTOs.
     *
     * @param plan the menu plan to get contents of.
     * @param day  the day for which to get contents of. (0 = first day, 1 = second day, etc.)
     * @return a set of menu plan content detail DTOs on the specified day. Can be empty if menu plan has no contents on that day.
     * @throws NotFoundException        if the menu plan specified in the argument list could not be found in the data store.
     * @throws IllegalArgumentException if the specified day is not within range of the menu plan (e.g. menu plan has 7 days, day = 10 would be invalid)
     * @author Marc Putz
     */
    Set<MenuPlanContentDetailDto> getContentsOfMenuPlanOnDayAsDetailDto(MenuPlan plan, int day) throws NotFoundException, IllegalArgumentException;

    /**
     * Gets the content of a menu plan by day and timeslot.
     *
     * @param plan     the menu plan to get the content of.
     * @param day      the day for which to get content of. (0 = first day, 1 = second day, etc.)
     * @param timeslot the timeslot of the day to get content of (0 = first slot, 1 = second slot, etc.)
     * @return the menu plan content specified on the given day and timeslot. Can be NULL if no content is specified.
     * @throws NotFoundException        if the menu plan specified in the argument list could not be found in the data store.
     * @throws IllegalArgumentException if the specified day is not within range of the menu plan (e.g. menu plan has 7 days, day = 10 would be invalid)
     * @author Marc Putz
     */
    MenuPlanContent getContentOfMenuPlanOnDayAndTimeslot(MenuPlan plan, int day, int timeslot) throws NotFoundException, IllegalArgumentException;

    /**
     * Gets the content of a menu plan by day and timeslot as its corresponding detail DTO.
     *
     * @param plan     the menu plan to get the content of.
     * @param day      the day for which to get content of. (0 = first day, 1 = second day, etc.)
     * @param timeslot the timeslot of the day to get content of (0 = first slot, 1 = second slot, etc.)
     * @return the menu plan content detail DTO specified on the given day and timeslot. Can be NULL if no content is specified.
     * @throws NotFoundException        if the menu plan specified in the argument list could not be found in the data store.
     * @throws IllegalArgumentException if the specified day is not within range of the menu plan (e.g. menu plan has 7 days, day = 10 would be invalid)
     * @author Marc Putz
     */
    MenuPlanContentDetailDto getContentOfMenuPlanOnDayAndTimeslotAsDetailDto(MenuPlan plan, int day, int timeslot)
        throws NotFoundException, IllegalArgumentException;

    /**
     * Gets the content of a menu plan by its ID class.
     *
     * @param contentId the ID class of the menu plan content containing the search parameters.
     * @return the menu plan content specified by the ID object. Can be NULL if no content is specified.
     * @throws NotFoundException        if the menu plan specified in the ID object could not be found in the data store.
     * @throws IllegalArgumentException if parameters in ID object are not within range of the menu plan (e.g. too many days, invalid timeslot, etc.)
     * @author Marc Putz
     */
    MenuPlanContent getContentOfMenuPlanById(MenuPlanContentId contentId) throws NotFoundException, IllegalArgumentException;

    /**
     * Gets the content of a menu plan by its ID class as its corresponding detail DTO.
     *
     * @param contentId the ID class of the menu plan content containing the search parameters.
     * @return the menu plan content detail DTO specified by the ID object. Can be NULL if no content is specified.
     * @throws NotFoundException        if the menu plan specified in the ID object could not be found in the data store.
     * @throws IllegalArgumentException if parameters in ID object are not within range of the menu plan (e.g. too many days, invalid timeslot, etc.)
     * @author Marc Putz
     */
    MenuPlanContentDetailDto getContentOfMenuPlanByIdAsDetailDto(MenuPlanContentId contentId) throws NotFoundException, IllegalArgumentException;

    /**
     * Creates the fridge for the menu plan.
     *
     * @param menuPlan needs to have valid id, everything else is irrelevant.
     * @param fridge   List of Ingredients which we add to the fridge.
     */
    void createFridge(MenuPlan menuPlan, List<String> fridge) throws ValidationException, ConflictException;

    /**
     * Checks the MenuPlan for running recipes and adds all the ingredients to the inventory.
     * Only creates the inventory for running MenuPlans, not outdated ones!
     *
     * @param user for which we want to create the inventory.
     */
    void createInventory(ApplicationUser user);

    /**
     * Searches the inventory for all ingredients matching the user.
     *
     * @param user      for which we want to get the inventory.
     * @param onlyValid if true we only look at the inventory for the MenuPlan which is still running. If false we return the whole inventory over all MenuPlans.
     * @return list of the inventory split into missing and available ingredients.
     */
    InventoryListDto searchInventory(ApplicationUser user, boolean onlyValid);

    /**
     * Returns the inventory for a specific MenuPlan.
     *
     * @param menuPlanId id of the MenuPlan which we want to lookup.
     * @return list of the inventory split into missing and available ingredients used in the specific MenuPlan.
     */
    InventoryListDto searchInventory(Long menuPlanId);

    /**
     * Updates a single inventory ingredient.
     *
     * @param user                 user of which we want to update an ingredient from
     * @param updatedIngredientDto updated inventory ingredient.
     * @throws NotFoundException if ingredient did not exist before
     * @throws ConflictException if ingredient was updated incorrectly
     */
    void updateInventoryIngredient(ApplicationUser user, InventoryIngredientDto updatedIngredientDto) throws NotFoundException, ConflictException;

    /**
     * Updates a single inventory ingredient. This should never be used in the endpoint for security reasons,
     * since we not validate if the user is authorized to change the inventory.
     *
     * @param updatedIngredientDto updated inventory ingredient.
     * @throws NotFoundException if ingredient did not exist before
     * @throws ConflictException if ingredient was updated incorrectly
     */
    void updateInventoryIngredient(InventoryIngredientDto updatedIngredientDto) throws NotFoundException, ConflictException;
}
