package com.b2mark.invoice;

import com.b2mark.invoice.config.CorsFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@SpringBootApplication
public class InvoiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(InvoiceApplication.class, args);
	}
	@Bean
	CorsFilter corsFilter() {
		CorsFilter filter = new CorsFilter();
		return filter;
	}

	@Bean
	public JavaMailSender getJavaMailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost("smtp.yandex.com");
		mailSender.setPort(587);

		mailSender.setUsername("no-reply@becopay.com");
		mailSender.setPassword("ioNatErLe");

		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.host", "smtp.yandex.com");
		props.put("mail.smtp.from", "no-reply@becopay.com");
		props.put("mail.smtp.user", "no-reply@becopay.com");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.debug", "false");

		return mailSender;
	}

}