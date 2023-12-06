package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ResetPasswordDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.PasswordResetRequest;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.PasswordResetRequestRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.PasswordResetService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.eclipse.angus.mail.util.MailConnectException;
import org.h2.engine.User;
import org.hibernate.LazyInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Properties;
import java.util.Random;

@Service
public class JpaPasswordResetService implements PasswordResetService {

    private static final String EMAIL_ADDRESS = "mail.bitebuddy@gmail.com";
    private static final String EMAIL_SMTP_HOST = "smtp.gmail.com";
    private static final int EMAIL_SMTP_PORT = 587;
    private static final String EMAIL_SMTP_USERNAME = "mail.bitebuddy@gmail.com";
    private static final String EMAIL_SMTP_PASSWORD = "nuem wfjl fhnz zrvd";
    private static final String RESET_LINK = "http://localhost:4200/password_reset";

    private final UserService userService;

    private final PasswordResetRequestRepository requestRepository;

    @Autowired
    public JpaPasswordResetService(UserService userService, PasswordResetRequestRepository requestRepository) {
        this.userService = userService;
        this.requestRepository = requestRepository;
    }

    @Override
    public void requestPasswordReset(String email) throws UserNotFoundException, MessagingException {
        ApplicationUser user = userService.getUserByEmail(email);
        requestPasswordReset(user);
    }

    @Override
    public void requestPasswordReset(ApplicationUser user) throws MessagingException {

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
        sendEmail(user.getEmail(), requestId, requestExpirationTime);
    }

    private void sendEmail(String recipientEmail, String requestId, LocalDateTime expirationTime) throws MessagingException {
        try {
            Properties prop = new Properties();
            prop.put("mail.smtp.auth", true);
            prop.put("mail.smtp.starttls.enable", "true");
            prop.put("mail.smtp.host", EMAIL_SMTP_HOST);
            prop.put("mail.smtp.port", EMAIL_SMTP_PORT);
            prop.put("mail.smtp.ssl.trust", "smtp.gmail.com");

            Session session = Session.getInstance(prop, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_SMTP_USERNAME, EMAIL_SMTP_PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_ADDRESS));
            message.setRecipients(
                Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Reset Password Confirmation");

            String resetLink = RESET_LINK + "?id=" + requestId +
                "&exp=" + expirationTime.getYear() + "-" + expirationTime.getDayOfYear() + "T" + expirationTime.getHour() + "." + expirationTime.getMinute();

            String msg =
                "<html><body><p>" +
                "A password reset was requested for '" + recipientEmail + "'.<br>" +
                "<br>" +
                "If this was not you, ignore this email and check your account.<br>" +
                "<br>" +
                "<strong>Follow this link to reset your password: </strong><br>" +
                "<a src='" + resetLink + "'>Reset Password</a><br>" +
                "<br>" +
                "If this link does not work for you, try using this link: <br>" +
                "<a src='" + resetLink + "'>" + resetLink + "</a><br>" +
                "</p></body></html>";

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(msg, "text/html; charset=utf-8");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);

            message.setContent(multipart);

            Transport.send(message);
        } catch (MailConnectException ex) {
            throw new MessagingException(ex.getMessage(), ex);
        }
    }

    @Override
    public void resetPassword(ResetPasswordDto dto) throws ValidationException {

        try {

            //get request entry from data store
            String encodedId = PasswordEncoder.encode(dto.getResetId(), "password_reset");
            PasswordResetRequest resetRequest = this.requestRepository.getReferenceById(encodedId);

            // TODO: check if request not expired (using resetRequest timestamp)

            // fetch user from request
            ApplicationUser userToChange = resetRequest.getUser();

            // save new password to user
            UserUpdateDto updateDto = UserUpdateDto.UserUpdateDtoBuilder.anUserUpdateDto()
                    .withPassword(dto.getNewPassword())
                    .withEmail(userToChange.getEmail())
                    .withName(userToChange.getNickname())
                    .build();
            userService.update(updateDto, userToChange.getId());

        } catch (LazyInitializationException ex) {
            // could not fetch request entry from data store
            // TODO: throw error
        } catch (UserNotFoundException ex) {
            // this should NEVER happen
            // TODO: throw error
        } catch (ConflictException ex) {
            // this should NEVER happen
            // TODO: throw errors
        }
    }
}
