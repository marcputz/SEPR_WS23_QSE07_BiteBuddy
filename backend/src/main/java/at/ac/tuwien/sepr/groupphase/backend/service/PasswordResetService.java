package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ResetPasswordDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import jakarta.mail.MessagingException;

public interface PasswordResetService {

    public void requestPasswordReset(String email) throws UserNotFoundException, MessagingException;

    public void requestPasswordReset(ApplicationUser user) throws MessagingException;

    public void resetPassword(ResetPasswordDto dto) throws ValidationException;
}
