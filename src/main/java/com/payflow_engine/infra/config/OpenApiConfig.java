package com.payflow_engine.infra.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(){
        return new OpenAPI().info(new Info()
                        .title("PayFlow Engine API")
                        .version("1.0.0")
                        .description("Motor de pagamentos assíncronos e carteira digital.")
                        .contact(new Contact().name("Erick Masson").email("erickzmasson@gmail.com")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")));
    }

    @Bean
    public OpenApiCustomizer openApiCustomizer(){
        return openApi-> {
            var loginOperation = openApi.getPaths().get("/api/v1/auth/login").getPost();
            if(loginOperation != null && loginOperation.getRequestBody() != null){
                loginOperation.getRequestBody().getContent().get("application/json")
                        .addExamples("default", new Example().value("""
                                {
                                  "email": "joao@email.com",
                                  "password": "senha123"
                                }
                                """));
            }

            var transferOperation = openApi.getPaths().get("/api/v1/transfers").getPost();
            if (transferOperation != null) {
                transferOperation.addParametersItem(new HeaderParameter()
                        .name("X-Idempotency-Key")
                        .description("Chave de idempotência para evitar duplicidade")
                        .required(true)
                        .example("chave-teste-001"));

                // Exemplo de body
                if (transferOperation.getRequestBody() != null) {
                    transferOperation.getRequestBody().getContent().get("application/json")
                            .addExamples("default", new Example().value("""
                                    {
                                      "payerId": 1,
                                      "payeeId": 2,
                                      "value": 50.00
                                    }
                                    """));
                }
            }
        };
    }
}
