package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.authentication.ResetPasswordDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.AuthenticationException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import jakarta.mail.MessagingException;

/**
 * Service class to handle password resets.
 *
 * @author Marc Putz
 */
public interface PasswordResetService {

    /**
     * Requests a password reset for a give user, identified by their email.
     * Method will log the reset request and send a verification email to the user.
     *
     * @param email the email of the user to request a password change
     * @throws NotFoundException If the email specified does not match a user in the data store
     * @throws MessagingException If the email service cannot send an email to the user
     * @author Marc Putz
     */
    void requestPasswordReset(String email) throws NotFoundException, MessagingException;

    /**
     * Performs a password reset with the user data given in the DTO object.
     * The DTO object needs to include a valid request ID, otherwise the password reset will be rejected
     *
     * @param dto a DTO object containing all data needed for a password reset
     * @throws AuthenticationException if the DTO contains an invalid request ID and the user cannot be identified
     * @throws ValidationException if the DTO contains invalid password data
     * @author Marc Putz
     */
    void resetPassword(ResetPasswordDto dto) throws AuthenticationException, ValidationException;
}
