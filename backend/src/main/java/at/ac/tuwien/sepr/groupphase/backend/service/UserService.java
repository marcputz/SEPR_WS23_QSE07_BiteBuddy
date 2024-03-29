package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserRegisterDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserUpdateEmailAndPasswordDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserUpdateSettingsDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.DataStoreException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;

/**
 * UserService interface to handle data storage for users.
 */
public interface UserService {

    /**
     * Gets the user details by the user's email.
     *
     * @param email                 the users email
     * @return                      the ApplicationUser entity
     * @throws NotFoundException    if the provided email does not match any user entry
     */
    ApplicationUser getUserByEmail(String email) throws NotFoundException;

    /**
     * Gets the user details by the user's nickname.
     *
     * @param nickname              the user's nickname
     * @return                      the ApplicationUser entity
     * @throws NotFoundException    if the provided nickname does not match any user entry
     */
    ApplicationUser getUserByNickname(String nickname) throws NotFoundException;

    /**
     * Retrieves an ApplicationUser by their ID.
     *
     * @param userId                the ID of the user
     * @return                      the ApplicationUser entity
     * @throws NotFoundException    if no user is found with the provided ID
     */
    ApplicationUser getUserById(Long userId) throws NotFoundException;

    ApplicationUser create(UserRegisterDto registerDto) throws DataStoreException, ConflictException, ValidationException;

    /**
     * Updates the email and password of an ApplicationUser based on the details provided in UserUpdateEmailAndPasswordDto.
     * Validates the existence of the user specified by currentUserId, checks for email uniqueness,
     * and updates the email and password in the database. This method ensures that only the user corresponding to currentUserId
     * can update their own email and password.
     *
     * @param dto                   The DTO containing the new email and password along with the current password for verification.
     * @param userId                The ID of the user currently logged in and requesting the update. This is used to ensure that only the authenticated user can update their own email and password.
     * @return                      The updated user object after successful persistence.
     * @throws NotFoundException    If no user is found with the provided currentUserId.
     * @throws ValidationException  If input validation fails.
     * @throws ConflictException    If the new email conflicts with another user's email.
     * @throws DataStoreException   If the data store is unable to process the request.
     */
    ApplicationUser updateEmailAndPassword(UserUpdateEmailAndPasswordDto dto, long userId)
        throws DataStoreException, NotFoundException, ValidationException, ConflictException;

    /**
     * Updates an ApplicationUser entity in the database with the details provided in the userToUpdate object.
     * Performs validations to ensure the user exists, and checks for conflicts such as duplicate emails or nicknames.
     * Also validates the user data according to the defined constraints.
     *
     * @param   userToUpdate        The ApplicationUser entity with updated information.
     * @return                      The updated ApplicationUser object after successful persistence.
     * @throws NotFoundException    If the user with the given ID does not exist in the database.
     * @throws ValidationException  If the provided user data does not meet validation criteria.
     * @throws ConflictException    If there are conflicts with existing data (e.g., duplicate email or nickname).
     * @throws DataStoreException   If the data store is unable to process the request.
     */
    ApplicationUser updateUser(ApplicationUser userToUpdate)
        throws DataStoreException, NotFoundException, ValidationException, ConflictException;

    /**
     * Updates user settings with the details provided in the parameter DTO.
     *
     * @param  dto                      a DTO containing the update information.
     * @param  userId                   the ID of the user to update.
     * @return                          the updated ApplicationUser entity.
     * @throws NotFoundException        if the user specified by {@code userId} could not be found.
     * @throws ValidationException      if the new data is invalid.
     * @throws ConflictException        if there are conflicts with existing data.
     * @throws DataStoreException       if the data store is unable to process the request.
     */
    ApplicationUser updateSettings(UserUpdateSettingsDto dto, long userId)
        throws DataStoreException, NotFoundException, ValidationException, ConflictException;
}
