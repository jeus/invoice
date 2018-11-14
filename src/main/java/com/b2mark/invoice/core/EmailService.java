/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    JavaMailSender javaMailSender;

    private TemplateEngine templateEngine;

    @Autowired
    public EmailService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    private String build(String themeName,Map<String ,Object> map) {
        Context context = new Context();
        context.setVariables(map);
        return templateEngine.process(themeName, context);
    }

    public void sendMail(String to,String themeName,Map<String ,Object> map) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        String html = build(themeName,map);
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setSubject("Becopay payment service [Payment Success]");
        helper.setTo(to);
        helper.setFrom("no-reply@becopay.com");
        helper.setText(html,true);
        javaMailSender.send(message);
    }

}
