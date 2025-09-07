package io.ghassen.pockito.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Configuration class for enhanced logging features in development environment.
 * Provides request/response logging and other debugging utilities.
 */
@Configuration
public class LoggingConfig {

    private static final Logger logger = LoggerFactory.getLogger(LoggingConfig.class);

    /**
     * Configures request logging filter for detailed HTTP request/response logging.
     * This filter logs the request method, URI, headers, and payload (if enabled).
     * 
     * @return CommonsRequestLoggingFilter configured for development logging
     */
    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        
        // Enable logging of request details
        loggingFilter.setIncludeClientInfo(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setIncludeHeaders(true);
        
        // Set maximum payload length to log (in characters)
        // Set to 1000 characters to avoid overwhelming logs with large payloads
        loggingFilter.setMaxPayloadLength(1000);
        
        // Set the message prefix for request logs
        loggingFilter.setBeforeMessagePrefix("REQUEST START: ");
        loggingFilter.setAfterMessagePrefix("REQUEST END: ");
        
        logger.info("Request logging filter configured for development environment");
        
        return loggingFilter;
    }
}
