package it.andrea.start.configuration;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApi30Config {

    @Value("${app.swagger.baseurl}")
    private String httpSwaggerServerUrl;

    @Value("${app.swagger.baseurl-https}")
    private String httpsSwaggerServerUrl;

    private static final String BEARER_AUTH_SCHEME_NAME = "bearerAuth";

    @Bean
    OpenAPI customOpenAPI() {
        // @formatter:off
        return new OpenAPI()
                .servers(Arrays.asList(createServer(httpSwaggerServerUrl), createServer(httpsSwaggerServerUrl)))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME_NAME))
                .components(
                        new Components().addSecuritySchemes(
                                BEARER_AUTH_SCHEME_NAME, new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .info(new Info().title("Base Template API Documentation").version("v1"));
        // @formatter:on
    }

    private Server createServer(String url) {
        return new Server().url(url);
    }

}
