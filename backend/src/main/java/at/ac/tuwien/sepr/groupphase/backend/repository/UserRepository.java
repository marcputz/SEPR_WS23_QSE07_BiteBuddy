package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

/**
 * UserRepository interface for handling ApplicationUser entities.
 * This interface extends JpaRepository, providing standard methods
 * for CRUD operations and additional custom queries for ApplicationUser entities.
 */
@Repository
public interface UserRepository extends JpaRepository<ApplicationUser, Long> {

    /**
     * Retrieves an ApplicationUser by their email.
     * This method returns an ApplicationUser whose email matches the provided value.
     * If no user is found with the given email, this method returns null.
     *
     * @param email The email address to search for.
     * @return The ApplicationUser with the specified email, or null if no such user exists.
     */
    ApplicationUser findUserByEmail(String email);
}
