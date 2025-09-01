package com.lucassousa.securedoc.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SecureDoc API")
                        .version("1.0")
                        .description("API for generating and verifying digital signatures using SHA-512 and RSA (CMS attached).")
                        .contact(new Contact().name("Lucas Sousa").email("lucas.sousa_99@hotmail.com"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project Challenge Description")
                        .url("https://github.com/SrSousa011/Java-back-end-developer-challenge"));
    }
}
