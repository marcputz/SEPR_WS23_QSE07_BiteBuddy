package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.MenuPlan;
import at.ac.tuwien.sepr.groupphase.backend.entity.MenuPlanContent;
import at.ac.tuwien.sepr.groupphase.backend.entity.Profile;
import at.ac.tuwien.sepr.groupphase.backend.entity.idclasses.MenuPlanContentId;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.DataStoreException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.MenuPlanService;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.util.List;

/**
 * JPA implementation of MenuPlanService interface.
 *
 * @author Marc Putz
 */
@Service
public class JpaMenuPlanService implements MenuPlanService {
    @Override
    public List<MenuPlan> getAllMenuPlansOfUser(ApplicationUser user) {
        throw new NotImplementedException();
    }

    @Override
    public List<MenuPlan> getAllMenuPlansOfUserDuringTimeframe(ApplicationUser user, LocalDate from, LocalDate until) {
        throw new NotImplementedException();
    }

    @Override
    public MenuPlan getMenuPlanForUserOnDate(ApplicationUser user, LocalDate date) {
        throw new NotImplementedException();
    }

    @Override
    public MenuPlan generateMenuPlan(ApplicationUser user, Profile profile, LocalDate from, LocalDate until) throws DataStoreException, ConflictException, ValidationException {
        throw new NotImplementedException();
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
    public List<MenuPlanContent> getContentsOfMenuPlan(MenuPlan plan) {
        throw new NotImplementedException();
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
