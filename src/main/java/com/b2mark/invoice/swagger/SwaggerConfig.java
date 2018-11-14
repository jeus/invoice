package com.b2mark.invoice.swagger;

import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Predicates;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.web.Link;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.AlternateTypeRule;
import springfox.documentation.schema.AlternateTypeRules;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Value("${info.app.name}")
    private String serviceName;
    @Value("${info.app.desc}")
    private String serviceDesc;
    @Value("${info.app.contact.email}")
    private String email;
    @Value("${info.app.contact.url}")
    private String url;
    @Value("${info.app.contact.name}")
    private String contactName;
    @Value("${info.app.version}")
    private String version;
    @Value("${info.app.license}")
    private String license;
    @Value("${uaa.clientId}")
    String clientId;
    @Value("${uaa.clientSecret}")
    String clientSecret;

    @Value("${uaa.url}")
    String oAuthServerUri;

    @Bean
    public Docket api() {
        final ApiInfo apiInfo = apiInfo();
        AlternateTypeRule typeRule1 = AlternateTypeRules.newRule(LocalDateTime.class, Date.class);



        return new Docket(DocumentationType.SWAGGER_2).protocols(Sets.newHashSet("https"))
                .alternateTypeRules(typeRule1)
                .select().apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .paths(Predicates.not(PathSelectors.regex("/error.*")))
                .build().apiInfo(apiInfo);
    }

    /**
     * disable validatorURL the section on swagger_ui that check all APIs.
     * security
     * @return
     */
    @Bean
    UiConfiguration uiConfig() {
        return UiConfigurationBuilder.builder()
                .displayRequestDuration(true)
                .validatorUrl("")
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title(serviceName)
                .description(serviceDesc)
                .version(version).license(license)
                .contact(new Contact(contactName,
                        url,
                        email))
                .termsOfServiceUrl("www.becopay.com/terms")
                .licenseUrl("www.becopay.com/license")
                .extensions(Collections.emptyList()).build();

    }


}

