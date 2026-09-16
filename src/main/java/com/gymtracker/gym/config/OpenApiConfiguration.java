package com.gymtracker.gym.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    private static final String OAUTH_SCHEME = "keycloak";
    private static final String BEARER_SCHEME = "bearerAuth";

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Bean
    public OpenAPI gymTrackerOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("GymTracker API")
                        .version("v1")
                        .description("""
                                Every /api/** endpoint requires a JWT issued by Keycloak.
                                Use the Authorize button: "keycloak" walks you through the login flow,
                                "bearerAuth" lets you paste an access token you already have."""))
                .addSecurityItem(new SecurityRequirement().addList(OAUTH_SCHEME))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(OAUTH_SCHEME, keycloakScheme())
                        .addSecuritySchemes(BEARER_SCHEME, bearerScheme()));
    }

    private SecurityScheme keycloakScheme() {
        OAuthFlow authorizationCode = new OAuthFlow()
                .authorizationUrl(issuerUri + "/protocol/openid-connect/auth")
                .tokenUrl(issuerUri + "/protocol/openid-connect/token")
                .scopes(new Scopes()
                        .addString("openid", "OpenID Connect")
                        .addString("profile", "User profile")
                        .addString("email", "Email address"));

        return new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .description("Log in through Keycloak (Authorization Code + PKCE)")
                .flows(new OAuthFlows().authorizationCode(authorizationCode));
    }

    private SecurityScheme bearerScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Raw access token, e.g. copied from the frontend's sessionStorage");
    }
}
