package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.authentication.ResetPasswordDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.PasswordResetRequest;
import at.ac.tuwien.sepr.groupphase.backend.exception.AuthenticationException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.PasswordResetRequestRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.EmailService;
import at.ac.tuwien.sepr.groupphase.backend.service.PasswordResetService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import jakarta.mail.MessagingException;
import org.hibernate.JDBCException;
import org.hibernate.LazyInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * Implementation of PasswordResetService using JPA.
 *
 * @author Marc Putz
 */
@Service
public class JpaPasswordResetService implements PasswordResetService {

    private static final String RESET_LINK = "http://localhost:4200/password_reset";

    private final UserService userService;

    private final EmailService emailService;

    private final PasswordResetRequestRepository requestRepository;

    @Autowired
    public JpaPasswordResetService(UserService userService, EmailService emailService, PasswordResetRequestRepository requestRepository) {
        this.userService = userService;
        this.emailService = emailService;
        this.requestRepository = requestRepository;
    }

    @Override
    public void requestPasswordReset(String email) throws NotFoundException, MessagingException {
        ApplicationUser user = userService.getUserByEmail(email);
        requestPasswordReset(user);
    }

    protected void requestPasswordReset(ApplicationUser user) throws MessagingException {

        // generate random request ID
        StringBuilder requestIdBuilder = new StringBuilder();
        String alphabet = "1234567890abcdefghijklmnopqrstuvwxyz";
        Random r = new Random();
        for (int i = 0; i < 32; i++) {
            requestIdBuilder.append(alphabet.charAt(r.nextInt(alphabet.length())));
        }
        String requestId = requestIdBuilder.toString();

        // encode request ID with password hash
        String encodedRequestId = PasswordEncoder.encode(requestId, "password_reset");

        // generate new entry in "passwordChangeRequest" table with generated hashed ID, user email and timestamp
        PasswordResetRequest resetRequest = new PasswordResetRequest();
        resetRequest.setId(encodedRequestId);
        resetRequest.setUser(user);
        resetRequest.setRequestTime(LocalDateTime.now());

        LocalDateTime requestExpirationTime = resetRequest.getRequestTime().plusHours(24);

        requestRepository.save(resetRequest);

        // send email to user with password reset link (attaching the ID as url parameter)
        sendResetRequestEmail(user.getEmail(), requestId, requestExpirationTime);
    }

    /**
     * Sends a request confirmation email to a recipient.
     *
     * @param recipient the email address of the recipient
     * @param requestId the reset request ID (not encoded)
     * @param expirationTime the expiration time of the reset request
     * @throws MessagingException if something went wrong while sending the email
     */
    protected void sendResetRequestEmail(String recipient, String requestId, LocalDateTime expirationTime) throws MessagingException {

        String mailSubject = "Reset Password Confirmation";

        String resetLink = RESET_LINK + "?id=" + requestId
            + "&exp=" + expirationTime.getYear() + "-" + expirationTime.getMonthValue() + "-" + expirationTime.getDayOfMonth() + "T" + expirationTime.getHour() + "." + expirationTime.getMinute();

        String content = "<html><body><p>"
            + "A password reset was requested for '" + recipient + "'.<br>"
            + "<br>"
            + "If this was not you, ignore this email and check your account.<br>"
            + "<br>"
            + "<strong>Follow this link to reset your password: </strong><br>"
            + "<a href='" + resetLink + "'>Reset Password</a><br>"
            + "<br>"
            + "If this link does not work for you, try using this link: <br>"
            + "<a href='" + resetLink + "'>" + resetLink + "</a><br>"
            + "</p></body></html>";

        this.emailService.sendEmail(recipient, mailSubject, content, true);

    }

    @Override
    public void resetPassword(ResetPasswordDto dto) throws NotFoundException, AuthenticationException, ValidationException {

        try {

            //get request entry from data store
            String encodedId = PasswordEncoder.encode(dto.getResetId(), "password_reset");
            PasswordResetRequest resetRequest = this.requestRepository.getReferenceById(encodedId);

            LocalDateTime requestTimestamp = resetRequest.getRequestTime();
            LocalDateTime expiredTime = requestTimestamp.plusHours(24);
            if (expiredTime.isBefore(LocalDateTime.now())) {
                // request has expired
                throw new AuthenticationException("Password Reset Request is expired");
            }

            // fetch user from request
            ApplicationUser userToChange = resetRequest.getUser();

            // save new password to user
            userToChange.setPasswordEncoded(PasswordEncoder.encode(dto.getNewPassword(), userToChange.getEmail()));
            userService.updateUser(userToChange);

        } catch (LazyInitializationException | JDBCException ex) {
            // could not fetch request entry from data store
            throw new NotFoundException("Request ID invalid or not found");
        } catch (NotFoundException ex) {
            // this should NEVER happen
            throw new AuthenticationException("Could not find user");
        } catch (ConflictException ex) {
            // this should NEVER happen
            throw new ValidationException("Data not valid", List.of("New data resulted in a conflict"));
        }
    }
}
