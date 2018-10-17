package org.pac4j.springframework.helper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

/**
 * The configuration for helpers.
 *
 * @author Jerome Leleu
 * @since 3.2.0
 */
@Configuration
public class HelperConfig {

    @Bean
    @RequestScope
    public UISecurityHelper uiSecurityHelper() {
        return new UISecurityHelper();
    }

    @Bean
    @RequestScope
    public WSSecurityHelper wsSecurityHelper() {
        return new WSSecurityHelper();
    }
}
