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
import at.ac.tuwien.sepr.groupphase.backend.utils.AuthTokenUtils;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
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

    @Autowired
    public MenuPlanEndpoint(MenuPlanService menuPlanService, UserService userService, ProfileService profileService, AuthenticationService authService) {
        this.service = menuPlanService;
        this.userService = userService;
        this.authService = authService;
        this.profileService = profileService;
    }

    /**
     * REST endpoint to generate a menu plan.
     *
     * @param headers the HTTP headers from the request.
     * @param dto the create dto to use for menu plan generation.
     * @return a detail dto of the created and generated menu plan.
     * @throws AuthenticationException if the user cannot be authenticated.
     * @throws ConflictException if the data given by the user is in conflict with the current state of the system.
     * @throws ValidationException if the data given by the user is invalid.
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

    @GetMapping("/forDate")
    @ResponseStatus(HttpStatus.OK)
    public MenuPlanDetailDto getMenuPlanOnDate(@RequestHeader HttpHeaders headers,
                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date)
        throws AuthenticationException, NotFoundException {
        LOGGER.trace("getMenuPlanOnDate({},{})", headers, date);

        authService.verifyAuthenticated(headers);

        // get user
        Long thisUserId = AuthTokenUtils.getUserId(authService.getAuthToken(headers));
        ApplicationUser thisUser = this.userService.getUserById(thisUserId);

        // generate menu plan
        return this.service.getMenuPlanForUserOnDateDetailDto(thisUser, date);
    }

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

    @PutMapping("/update")
    @ResponseStatus(HttpStatus.OK)
    public void updateRecipeInMenuPlan(@RequestHeader HttpHeaders headers, @RequestBody MenuPlanUpdateRecipeDto menuPlan)
        throws AuthenticationException, NotFoundException {
        LOGGER.info("updateRecipe({},{})", headers, menuPlan);

        this.authService.verifyAuthenticated(headers);

        Long thisUserId = AuthTokenUtils.getUserId(authService.getAuthToken(headers));
        ApplicationUser thisUser = this.userService.getUserById(thisUserId);

        this.service.updateMenuPlanByChangingOneRecipe(thisUser, menuPlan);
    }

    @GetMapping("/inventory/create/")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryListDto createInventory(@RequestHeader HttpHeaders headers) throws NotFoundException, AuthenticationException {
        // TODO this will be removed soon
        LOGGER.trace("createInventory({})", headers);

        this.authService.verifyAuthenticated(headers);

        Long thisUserId = AuthTokenUtils.getUserId(authService.getAuthToken(headers));
        ApplicationUser thisUser = this.userService.getUserById(thisUserId);

        this.service.createInventory(thisUser);
        return this.service.searchInventory(thisUser, true);
    }

    @GetMapping("/inventory/")
    @ResponseStatus(HttpStatus.OK)
    public InventoryListDto getInventory(@RequestHeader HttpHeaders headers) throws AuthenticationException, NotFoundException {
        LOGGER.trace("getInventory({})", headers);

        this.authService.verifyAuthenticated(headers);

        Long thisUserId = AuthTokenUtils.getUserId(authService.getAuthToken(headers));
        ApplicationUser thisUser = this.userService.getUserById(thisUserId);

        return this.service.searchInventory(thisUser, true);
    }

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
