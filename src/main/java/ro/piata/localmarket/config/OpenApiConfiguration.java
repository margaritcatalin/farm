package ro.piata.localmarket.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import ro.piata.localmarket.config.apidoc.customizer.LocalMarketOpenApiCustomizer;

@Configuration
@Profile(ConfigConstants.SPRING_PROFILE_API_DOCS)
public class OpenApiConfiguration {


    @Bean
    public GroupedOpenApi apiFirstGroupedOpenAPI(LocalMarketOpenApiCustomizer customizer) {
        return GroupedOpenApi.builder()
            .group("default")
            .pathsToMatch("/api/**")
            .addOpenApiCustomizer(customizer)
            .build();
    }

    @Bean
    public LocalMarketOpenApiCustomizer customizer(LocalMarketProperties properties) {
        return new LocalMarketOpenApiCustomizer(properties.getApiDocs());
    }
}
