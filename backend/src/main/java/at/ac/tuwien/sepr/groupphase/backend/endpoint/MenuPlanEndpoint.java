package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InventoryIngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InventoryListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan.MenuPlanCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan.MenuPlanDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.MenuPlan;
import at.ac.tuwien.sepr.groupphase.backend.entity.Profile;
import at.ac.tuwien.sepr.groupphase.backend.exception.AuthenticationException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotFoundException;
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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
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
    public MenuPlanDetailDto generateMenuPlan(@RequestHeader HttpHeaders headers, @RequestBody @Valid MenuPlanCreateDto dto) throws AuthenticationException, ConflictException, ValidationException {
        authService.verifyAuthenticated(headers);

        try {
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

        } catch (UserNotFoundException e) {
            // this should not happen as the authService verifies the logged-in user
            throw new AuthenticationException("User not found/authorized");
        } catch (NotFoundException e) {
            // profile not found
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find Profile with ID " + dto.getProfileId(), e);
        }
    }

    @GetMapping("/forDate")
    public MenuPlanDetailDto getMenuPlanDetails(@RequestHeader HttpHeaders headers, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) throws AuthenticationException {
        authService.verifyAuthenticated(headers);
        LOGGER.info("date: " + date.toString());
        try {
            // get user
            Long thisUserId = AuthTokenUtils.getUserId(authService.getAuthToken(headers));
            ApplicationUser thisUser = this.userService.getUserById(thisUserId);

            // generate menu plan
            return this.service.getMenuPlanForUserOnDateDetailDto(thisUser, date);

        } catch (UserNotFoundException e) {
            // this should not happen as the authService verifies the logged-in user
            LOGGER.warn("Error processing user data, user not found: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing user data", e);
        }
    }

    @GetMapping()
    public List<MenuPlanDetailDto> getMenuPlans(@RequestHeader HttpHeaders headers) throws AuthenticationException {
        LOGGER.info("Get menuplans called in backend");
        authService.verifyAuthenticated(headers);
        try {
            // get user
            Long thisUserId = AuthTokenUtils.getUserId(authService.getAuthToken(headers));
            ApplicationUser thisUser = this.userService.getUserById(thisUserId);

            // generate menu plan
            return this.service.getAllMenuPlansofUserDetailDto(thisUser);

        } catch (UserNotFoundException e) {
            // this should not happen as the authService verifies the logged-in user
            LOGGER.warn("Error processing user data, user not found: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing user data", e);
        }
    }

    @GetMapping("/inventory/create/")
    public InventoryListDto createInventory(@RequestHeader HttpHeaders headers) throws UserNotFoundException, AuthenticationException {
        // TODO this will be removed soon
        LOGGER.trace("createInventory()");

        this.authService.verifyAuthenticated(headers);
        String authToken = headers.getFirst("Authorization");
        Long currentUserId = AuthTokenUtils.getUserId(authToken);
        ApplicationUser user = this.userService.getUserById(currentUserId);

        this.service.createInventory(user);
        return this.service.searchInventory(user, true);
    }

    @GetMapping("/inventory/")
    public ResponseEntity<InventoryListDto> getInventory(@RequestHeader HttpHeaders headers) throws AuthenticationException, UserNotFoundException {
        LOGGER.trace("getInventory()");
        this.authService.verifyAuthenticated(headers);
        String authToken = headers.getFirst("Authorization");
        Long currentUserId = AuthTokenUtils.getUserId(authToken);
        ApplicationUser user = this.userService.getUserById(currentUserId);

        return ResponseEntity.ok(this.service.searchInventory(user, true));
    }

    @PutMapping("/inventory/update")
    public void updateInventoryIngredient(@RequestHeader HttpHeaders headers, @RequestBody InventoryIngredientDto updatedIngredient)
        throws AuthenticationException, UserNotFoundException, ConflictException {
        LOGGER.trace("update({})", updatedIngredient);
        this.authService.verifyAuthenticated(headers);
        String authToken = headers.getFirst("Authorization");
        Long currentUserId = AuthTokenUtils.getUserId(authToken);
        ApplicationUser user = this.userService.getUserById(currentUserId);

        this.service.updateInventoryIngredient(user, updatedIngredient);
    }
}
