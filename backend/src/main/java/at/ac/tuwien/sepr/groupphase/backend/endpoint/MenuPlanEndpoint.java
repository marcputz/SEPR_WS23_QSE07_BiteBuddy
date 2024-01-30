package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InventoryIngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InventoryListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan.MenuPlanCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan.MenuPlanDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan.MenuPlanUpdateRecipeDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.MenuPlan;
import at.ac.tuwien.sepr.groupphase.backend.entity.Profile;
import at.ac.tuwien.sepr.groupphase.backend.exception.AuthenticationException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthenticationService;
import at.ac.tuwien.sepr.groupphase.backend.service.MenuPlanService;
import at.ac.tuwien.sepr.groupphase.backend.service.ProfileService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.validation.MenuPlanValidator;
import at.ac.tuwien.sepr.groupphase.backend.utils.AuthTokenUtils;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * REST Endpoint for Menu Plan (and Inventory) methods.
 */
@RestController
@RequestMapping(value = MenuPlanEndpoint.BASE_PATH)
public class MenuPlanEndpoint {

    public static final String BASE_PATH = "/api/v1/menuplan";
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final MenuPlanService service;
    private final AuthenticationService authService;
    private final UserService userService;
    private final ProfileService profileService;
    private final MenuPlanValidator menuPlanValidator;

    @Autowired
    public MenuPlanEndpoint(MenuPlanService menuPlanService, UserService userService, ProfileService profileService, AuthenticationService authService, MenuPlanValidator menuPlanValidator) {
        this.service = menuPlanService;
        this.userService = userService;
        this.authService = authService;
        this.profileService = profileService;
        this.menuPlanValidator = menuPlanValidator;
    }

    /**
     * REST endpoint to generate a menu plan.
     *
     * @param headers the HTTP headers from the request.
     * @param dto     a MenuPlanCreateDto to use for menu plan generation.
     * @return a detail dto of the created and generated menu plan.
     * @throws AuthenticationException if the user cannot be authenticated.
     * @throws ConflictException       if the data given by the user is in conflict with the current state of the system.
     * @throws ValidationException     if the data given by the user is invalid.
     * @author Marc Putz
     */
    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public MenuPlanDetailDto generateMenuPlan(@RequestHeader HttpHeaders headers, @RequestBody @Valid MenuPlanCreateDto dto)
        throws AuthenticationException, ConflictException, ValidationException, NotFoundException {
        LOGGER.trace("generateMenuPlan({},{})", headers, dto);

        authService.verifyAuthenticated(headers);

        // get user
        Long thisUserId = AuthTokenUtils.getUserId(authService.getAuthToken(headers));
        ApplicationUser thisUser = this.userService.getUserById(thisUserId);

        // get profile
        Profile profile = this.profileService.getById(dto.getProfileId());

        // create menu plan
        MenuPlan newPlan = this.service.createEmptyMenuPlan(thisUser, profile, dto.getFromTime(), dto.getUntilTime());

        // saving fridge
        this.service.createFridge(newPlan, dto.getFridge());

        try {
            // generate menu plan content and return
            return this.service.generateContent(newPlan);
        } catch (Exception ex) {
            // if anything goes wrong on content creation, delete the menu plan
            this.service.deleteMenuPlan(newPlan);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }


    /**
     * REST endpoint to find a menu plan which start and endDate encompass the given date.
     *
     * @param headers the HTTP headers from the request.
     * @param date    a LocalDate to use for finding a menu plan.
     * @return a detail dto of the found menu plan.
     * @throws AuthenticationException if the user cannot be authenticated.
     * @throws NotFoundException       if there is no menu plan which start and endDate encompass the given date
     * @throws ValidationException     if the date is null
     * @author Anton Nather
     */
    @GetMapping("/forDate")
    @ResponseStatus(HttpStatus.OK)
    public MenuPlanDetailDto getMenuPlanOnDate(@RequestHeader HttpHeaders headers,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date)
        throws AuthenticationException, NotFoundException, ValidationException {
        LOGGER.trace("getMenuPlanOnDate({},{})", headers, date);

        authService.verifyAuthenticated(headers);

        // get user
        Long thisUserId = AuthTokenUtils.getUserId(authService.getAuthToken(headers));
        ApplicationUser thisUser = this.userService.getUserById(thisUserId);

        if (date == null) {
            throw new ValidationException("Date is null", new ArrayList<>());
        }
        // generate menu plan
        return this.service.getMenuPlanForUserOnDateDetailDto(thisUser, date);
    }

    /**
     * REST endpoint to return information about all menu plans of the current user.
     *
     * @param headers the HTTP headers from the request.
     * @return a List of menu plan detail dtos of this user.
     * @throws AuthenticationException if the user cannot be authenticated.
     * @throws NotFoundException       if the current user cannot be found in the DB
     * @author Anton Nather
     */
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<MenuPlanDetailDto> getMenuPlans(@RequestHeader HttpHeaders headers) throws AuthenticationException, NotFoundException {
        LOGGER.trace("getMenuPlans({})", headers);

        authService.verifyAuthenticated(headers);

        // get user
        Long thisUserId = AuthTokenUtils.getUserId(authService.getAuthToken(headers));
        ApplicationUser thisUser = this.userService.getUserById(thisUserId);

        // generate menu plan
        return this.service.getAllMenuPlansofUserDetailDto(thisUser);
    }

    /**
     * REST endpoint to update one Recipe in a menuplan of the current user.
     *
     * @param headers  the HTTP headers from the request.
     * @param menuPlan a MenuPlanUpdateRecipeDto to use for updating
     * @throws AuthenticationException if the user cannot be authenticated.
     * @throws ValidationException     if the menu plan dto is not valid
     * @author Anton Nather
     */
    @PutMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public void updateRecipeInMenuPlan(@RequestHeader HttpHeaders headers, @RequestBody MenuPlanUpdateRecipeDto menuPlan)
        throws AuthenticationException, ValidationException {
        LOGGER.info("updateRecipe({},{})", headers, menuPlan);

        this.authService.verifyAuthenticated(headers);

        Long thisUserId = AuthTokenUtils.getUserId(authService.getAuthToken(headers));
        ApplicationUser thisUser = this.userService.getUserById(thisUserId);
        menuPlanValidator.validateForUpdateOneRecipe(menuPlan);

        this.service.updateMenuPlanByChangingOneRecipe(thisUser, menuPlan);
    }

    /**
     * Gets the inventory of the current menu plan of the logged-in user.
     *
     * @param headers {@link HttpHeaders} with auth token.
     * @return {@link InventoryListDto} with all the missing and available inventory ingredients.
     * @throws AuthenticationException if the user cannot be authenticated.
     * @throws NotFoundException       if the user is not found.
     * @author Frederik Skiera
     */
    @GetMapping("/inventory/")
    @ResponseStatus(HttpStatus.OK)
    public InventoryListDto getInventory(@RequestHeader HttpHeaders headers) throws AuthenticationException, NotFoundException {
        LOGGER.trace("getInventory({})", headers);

        this.authService.verifyAuthenticated(headers);

        Long thisUserId = AuthTokenUtils.getUserId(authService.getAuthToken(headers));
        ApplicationUser thisUser = this.userService.getUserById(thisUserId);

        return this.service.searchInventory(thisUser, true);
    }

    /**
     * Updates an ingredient in the inventory.
     * This only works with valid ingredients and when logged in.
     *
     * @param headers           {@link HttpHeaders} with auth token.
     * @param updatedIngredient {@link InventoryIngredientDto} with updated inventory status.
     * @throws AuthenticationException if the user cannot be authenticated.
     * @throws ConflictException       if the data given by the user is in conflict with the current state of the system.
     * @throws ValidationException     if the data given by the user is invalid.
     * @throws NotFoundException       if the user is not found.
     * @author Frederik Skiera
     */
    @PutMapping("/inventory/update")
    @ResponseStatus(HttpStatus.OK)
    public void updateInventoryIngredient(@RequestHeader HttpHeaders headers, @RequestBody InventoryIngredientDto updatedIngredient)
        throws AuthenticationException, NotFoundException, ConflictException, ValidationException {
        LOGGER.trace("update({},{})", headers, updatedIngredient);

        this.authService.verifyAuthenticated(headers);

        Long thisUserId = AuthTokenUtils.getUserId(authService.getAuthToken(headers));
        ApplicationUser thisUser = this.userService.getUserById(thisUserId);

        this.service.updateInventoryIngredient(thisUser, updatedIngredient);
    }
}
