package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.MenuPlan;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuPlanRepository extends JpaRepository<MenuPlan, Long> {

    @Transactional
    void deleteAllByUser(ApplicationUser user);

    MenuPlan getAllByUser(ApplicationUser user);
}
