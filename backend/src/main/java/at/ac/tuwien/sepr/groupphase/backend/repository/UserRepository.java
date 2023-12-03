package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * UserRepository interface for handling ApplicationUser entities.
 * This interface extends JpaRepository, providing standard methods
 * for CRUD operations and additional custom queries for ApplicationUser entities.
 */
@Repository
public interface UserRepository extends JpaRepository<ApplicationUser, Long> {

    /**
     * Retrieves all users which have an ID less than the specified value.
     *
     * @param id the ID number to compare to
     * @return a list of users which have IDs less than {@code id}
     */
    List<ApplicationUser> findByIdLessThan(long id);

    /**
     * Retrieves an ApplicationUser by their email.
     * This method returns an ApplicationUser whose email matches the provided value.
     * If no user is found with the given email, this method returns null.
     *
     * @param email The email address to search for
     * @return The ApplicationUser with the specified email, or null if no such user exists
     */
    ApplicationUser findByEmail(String email);

    /**
     * Retrieves an ApplicationUser by their email, ignoring the case of the provided value.
     * This method returns an ApplicationUser whose email matches the provided value.
     * If no user is found with the given email, this method returns null.
     *
     * @param email The email address to search for, not case-sensitive
     * @return The ApplicationUser with the specified email, or null if no such user exists
     */
    ApplicationUser findByEmailIgnoreCase(String email);

    /**
     * Retrieves an ApplicationUser by their nickname.
     * This method returns an ApplicationUser whose nickname matches the provided value.
     * If no user is found with the given nickname, this method returns null.
     *
     * @param nickname the nickname to search for
     * @return The ApplicationUser with the specified email, or null if no such user exists
     */
    ApplicationUser findByNickname(String nickname);
}
