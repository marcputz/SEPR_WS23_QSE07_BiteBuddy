package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UserRepository interface for handling ApplicationUser entities.
 * This interface extends JpaRepository, providing standard methods
 * for CRUD operations and additional custom queries for ApplicationUser entities.
 */
@Repository
public interface UserRepository extends JpaRepository<ApplicationUser, Long> {

    /**
     * Retrieves an ApplicationUser by their email.
     *
     * @param email The email address to search for.
     * @return An optional for the ApplicationUser entity, which may be empty if no such user was found.
     */
    Optional<ApplicationUser> findByEmail(String email);

    /**
     * Retrieves an ApplicationUser by their email, ignoring the case of the provided value.
     *
     * @param email The email address to search for, not case-sensitive
     * @return An optional for the ApplicationUser entity, which may be empty if no such user was found.
     */
    Optional<ApplicationUser> findByEmailIgnoreCase(String email);

    /**
     * Retrieves an ApplicationUser by their nickname.
     * This method returns an ApplicationUser whose nickname matches the provided value.
     * If no user is found with the given nickname, this method returns null.
     *
     * @param nickname the nickname to search for
     * @return The ApplicationUser with the specified email, or null if no such user exists
     */
    Optional<ApplicationUser> findByNickname(String nickname);
}
