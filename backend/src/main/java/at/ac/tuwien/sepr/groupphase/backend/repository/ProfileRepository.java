package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing {@link Profile} entities.
 */
@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    List<Profile> getAllByUser(ApplicationUser user);
}
