package ro.piata.localmarket.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Local Market Application.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 * See {@link LocalMarketProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {


}
