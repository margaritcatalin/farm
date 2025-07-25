package ro.piata.localmarket.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.server.WebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class WebConfigurer implements WebServerFactoryCustomizer<WebServerFactory> {

    private static final Logger LOG = LoggerFactory.getLogger(WebConfigurer.class);

    private final Environment env;
    private final LocalMarketProperties localMarketProperties;

    public WebConfigurer(Environment env, LocalMarketProperties localMarketProperties) {
        this.env = env;
        this.localMarketProperties = localMarketProperties;
    }

    @Override
    public void customize(WebServerFactory server) {
        // No-op: static resource root removed for API-only mode
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = localMarketProperties.getCors();
        if (!CollectionUtils.isEmpty(config.getAllowedOrigins()) || !CollectionUtils.isEmpty(config.getAllowedOriginPatterns())) {
            LOG.debug("Registering CORS filter");
            source.registerCorsConfiguration("/api/**", config);
            source.registerCorsConfiguration("/management/**", config);
            source.registerCorsConfiguration("/v3/api-docs", config);
            source.registerCorsConfiguration("/swagger-ui/**", config);
        }
        return new CorsFilter(source);
    }
}
