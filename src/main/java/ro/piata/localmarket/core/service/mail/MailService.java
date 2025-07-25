package ro.piata.localmarket.core.service.mail;

import org.springframework.stereotype.Service;
import ro.piata.localmarket.core.model.user.User;

@Service
public interface MailService {

    void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml);


    void sendEmailFromTemplate(User user, String templateName, String titleKey);

    void sendActivationEmail(User user);

    void sendCreationEmail(User user);

    void sendPasswordResetMail(User user);
}
