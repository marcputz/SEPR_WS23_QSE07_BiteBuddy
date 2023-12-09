package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.config.ConfigProperties;
import at.ac.tuwien.sepr.groupphase.backend.service.EmailService;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.eclipse.angus.mail.util.MailConnectException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class SmtpEmailService implements EmailService {

    private final ConfigProperties config;

    @Autowired
    public SmtpEmailService(ConfigProperties config) {
        this.config = config;
    }

    @Override
    public boolean sendEmail(String recipient, String mailSubject, String mailContent, boolean contentIsHtml) throws MessagingException {

        String emailAddress = config.getEmail().getAddress();
        String smtpHost = config.getEmail().getSmtp().getHost();
        int smtpPort = config.getEmail().getSmtp().getPort();
        String smtpUsername = config.getEmail().getSmtp().getUsername();
        String smtpPassword = config.getEmail().getSmtp().getPassword();

        Properties prop = new Properties();
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", smtpHost);
        prop.put("mail.smtp.port", smtpPort);
        prop.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUsername, smtpPassword);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(emailAddress));
        message.setRecipients(
            Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject(mailSubject);

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(mailContent, contentIsHtml ? "text/html; charset=utf-8" : "text/plain; charset=utf-8");
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        message.setContent(multipart);

        Transport.send(message);

        return true;
    }

}
