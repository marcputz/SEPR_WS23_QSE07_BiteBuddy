package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan.MenuPlanCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan.MenuPlanDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.AuthenticationException;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthenticationService;
import at.ac.tuwien.sepr.groupphase.backend.service.MenuPlanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;

@RestController
@RequestMapping(value = MenuPlanEndpoint.BASE_PATH)
public class MenuPlanEndpoint {

    public static final String BASE_PATH = "/api/v1/menuplan";
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final MenuPlanService service;
    private final AuthenticationService authService;

    @Autowired
    public MenuPlanEndpoint(MenuPlanService menuPlanService, AuthenticationService authService) {
        this.service = menuPlanService;
        this.authService = authService;
    }

    @PostMapping("/generate")
    public MenuPlanDetailDto generateMenuPlan(@RequestHeader HttpHeaders headers, @RequestBody MenuPlanCreateDto dto) throws AuthenticationException {
        authService.verifyAuthenticated(headers);

        // TODO: generate Menu Plan
        System.out.println(dto);

        /** TEMP **/
        return new MenuPlanDetailDto()
            .setFromTime(LocalDate.now())
            .setUntilTime(LocalDate.now().plusDays(6))
            .setNumDays(7)
            .setProfileId(-1L)
            .setProfileName("TEST PROFILE")
            .setUserId(-1L);
    }
}
