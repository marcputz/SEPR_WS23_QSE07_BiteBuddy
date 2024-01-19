package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InventoryIngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InventoryListDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.AuthenticationException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthenticationService;
import at.ac.tuwien.sepr.groupphase.backend.service.InventoryIngredientService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.JpaUserService;
import at.ac.tuwien.sepr.groupphase.backend.utils.AuthTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.lang.invoke.MethodHandles;

@RestController
@RequestMapping(value = InventoryEndpoint.BASE_PATH)
public class InventoryEndpoint {
    public static final String BASE_PATH = "/api/v1/inventory";
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final InventoryIngredientService service;
    private final AuthenticationService authenticationService;
    private final JpaUserService userService;

    @Autowired
    public InventoryEndpoint(InventoryIngredientService service, AuthenticationService authenticationService, JpaUserService userService) {
        this.service = service;
        this.authenticationService = authenticationService;
        this.userService = userService;
    }

    @GetMapping("/create")
    public void createInventory(@RequestHeader HttpHeaders headers) throws UserNotFoundException, AuthenticationException {
        this.authenticationService.verifyAuthenticated(headers);
        String authToken = headers.getFirst("Authorization");
        Long currentUserId = AuthTokenUtils.getUserId(authToken);
        ApplicationUser user = this.userService.getUserById(currentUserId);

        this.service.createInventory(user);
    }

    @GetMapping("/")
    public InventoryListDto getInventory(@RequestHeader HttpHeaders headers) throws AuthenticationException, UserNotFoundException {
        LOGGER.trace("getInventory()");
        this.authenticationService.verifyAuthenticated(headers);
        String authToken = headers.getFirst("Authorization");
        Long currentUserId = AuthTokenUtils.getUserId(authToken);
        ApplicationUser user = this.userService.getUserById(currentUserId);

        if (currentUserId != null) {
            return this.service.searchInventory(user, true);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/update")
    public void updateInventoryIngredient(@RequestHeader HttpHeaders headers, @RequestBody InventoryIngredientDto updatedIngredient)
        throws AuthenticationException, UserNotFoundException, ConflictException {
        LOGGER.trace("update({})", updatedIngredient);
        this.authenticationService.verifyAuthenticated(headers);
        String authToken = headers.getFirst("Authorization");
        Long currentUserId = AuthTokenUtils.getUserId(authToken);
        ApplicationUser user = this.userService.getUserById(currentUserId);

        this.service.updateInventoryIngredient(user, updatedIngredient);
    }
}
