package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserRegisterDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;

/**
 * UserService interface to handle data storage for users.
 */
public interface UserService {

    /**
     * Gets the user details by the user's email.
     *
     * @param email the users email
     * @return the ApplicationUser entity
     * @throws UserNotFoundException if the provided email does not match any user entry
     */
    ApplicationUser getUserByEmail(String email) throws UserNotFoundException;

    /**
     * Gets the user details by the user's nickname.
     *
     * @param nickname the user's nickname
     * @return the ApplicationUser entity
     * @throws UserNotFoundException if the provided nickname does not match any user entry
     */
    ApplicationUser getUserByNickname(String nickname) throws UserNotFoundException;

    /**
     * Retrieves an ApplicationUser by their ID.
     *
     * @param userId the ID of the user
     * @return the ApplicationUser entity
     * @throws UserNotFoundException if no user is found with the provided ID
     */
    ApplicationUser getUserById(Long userId) throws UserNotFoundException;

    /**
     * Updates an ApplicationUser with the details provided in UserUpdateDto.
     * Validates the existence of the user specified by currentUserId, checks for email and nickname uniqueness,
     * and updates user data in the database. This method ensures that only the user corresponding to currentUserId
     * can update their own profile.
     *
     * @param userUpdateDto The DTO containing updated information for the user, including ID, email, nickname, and encoded password.
     * @param currentUserId The ID of the user currently logged in and requesting the update. This is used to ensure that
     *                      only the authenticated user can update their own information.
     * @return ApplicationUser The updated user object after successful persistence.
     * @throws UserNotFoundException If no user is found with the provided currentUserId.
     * @throws ValidationException   If input validation fails.
     * @throws ConflictException     If email or nickname conflicts are found with existing users.
     */
    ApplicationUser update(UserUpdateDto userUpdateDto, Long currentUserId) throws UserNotFoundException, ValidationException, ConflictException;

    ApplicationUser create(UserRegisterDto registerDto) throws ValidationException;
}
