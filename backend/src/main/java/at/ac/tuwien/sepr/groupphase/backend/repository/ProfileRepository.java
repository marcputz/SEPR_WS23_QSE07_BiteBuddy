package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing {@link Profile} entities.
 */
@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    @Query("SELECT p FROM Profile p WHERE "
        + "LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND "
        + "LOWER(p.user.nickname) LIKE LOWER(CONCAT('%', :creator, '%')) AND "
        + "p.user.id <> :userId")
    Page<Profile> findByNameContainingIgnoreCaseAndCreatorAndNotUserId(
        @Param("name") String name,
        @Param("creator") String creator,
        @Param("userId") Long userId,
        Pageable pageable);

    Page<Profile> findByNameContainingIgnoreCaseAndUserId(String name, Long userId, Pageable pageable);

}
