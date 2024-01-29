package at.ac.tuwien.sepr.groupphase.backend.service;

import jakarta.mail.MessagingException;

/**
 * Service class to handle sending emails.
 */
public interface EmailService {

    /**
     * Sends an email with specified subject line and content to a recipient.
     *
     * @param recipient the email address of the recipient
     * @param mailSubject the mail's subject line
     * @param mailContent the mail's content
     * @param contentIsHtml if {@code true}, mailContent is in HTML format, if {@code false}, emailContent is plain text
     * @throws MessagingException if the email could not be sent
     */
    void sendEmail(String recipient, String mailSubject, String mailContent, boolean contentIsHtml) throws MessagingException;

}
