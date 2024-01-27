package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.MenuPlan;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MenuPlanRepository extends JpaRepository<MenuPlan, Long> {

    @Transactional
    void deleteAllByUser(ApplicationUser user);

    Optional<MenuPlan> findById(Long id);

    List<MenuPlan> getAllByUser(ApplicationUser user);

    @Query("select m from MenuPlan m where m.user = :user and ((m.fromDate between :fromDate and :untilDate) or (m.untilDate between :fromDate and :untilDate))")
    List<MenuPlan> getAllByUserMatchingTimeframe(@Param("user") ApplicationUser user, @Param("fromDate") LocalDate fromDate, @Param("untilDate") LocalDate untilDate);

    @Query("select m from MenuPlan m where m.user = :user and m.fromDate <= :date and m.untilDate >= :date")
    MenuPlan getByUserOnDate(@Param("user") ApplicationUser user, @Param("date") LocalDate date);
}
