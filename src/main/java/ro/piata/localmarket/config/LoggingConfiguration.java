package ro.piata.localmarket.config;

import ch.qos.logback.classic.LoggerContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static ro.piata.localmarket.config.logging.LoggingUtils.addContextListener;
import static ro.piata.localmarket.config.logging.LoggingUtils.addJsonConsoleAppender;
import static ro.piata.localmarket.config.logging.LoggingUtils.addLogstashTcpSocketAppender;

/*
 * Configures the console and Logstash log appenders from the app properties
 */
@Configuration
public class LoggingConfiguration {

    public LoggingConfiguration(
            @Value("${spring.application.name}") String appName,
            @Value("${server.port}") String serverPort,
            LocalMarketProperties LocalMarketProperties,
            ObjectMapper mapper
    ) throws JsonProcessingException {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        Map<String, String> map = new HashMap<>();
        map.put("app_name", appName);
        map.put("app_port", serverPort);
        String customFields = mapper.writeValueAsString(map);

        LocalMarketProperties.Logging loggingProperties = LocalMarketProperties.getLogging();
        LocalMarketProperties.Logging.Logstash logstashProperties = loggingProperties.getLogstash();

        if (loggingProperties.isUseJsonFormat()) {
            addJsonConsoleAppender(context, customFields);
        }
        if (logstashProperties.isEnabled()) {
            addLogstashTcpSocketAppender(context, customFields, logstashProperties);
        }
        if (loggingProperties.isUseJsonFormat() || logstashProperties.isEnabled()) {
            addContextListener(context, customFields, loggingProperties);
        }
    }
}