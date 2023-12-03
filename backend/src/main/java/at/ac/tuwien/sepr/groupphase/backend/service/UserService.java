package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserRegisterDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.exception.UserNotFoundException;

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

    ApplicationUser update(ApplicationUser applicationUser) throws UserNotFoundException, ValidationException, ConflictException;

    ApplicationUser create(UserRegisterDto registerDto);

    // TODO: createUser, updateUser, deleteUser
}
