package com.b2mark.invoice;

import com.b2mark.invoice.config.CorsFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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
}