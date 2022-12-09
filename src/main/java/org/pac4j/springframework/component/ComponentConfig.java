package org.pac4j.springframework.component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jee.config.Pac4jJEEConfig;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

/**
 * The configuration of the pac4j components.
 *
 * @author Jerome Leleu
 * @since 3.2.0
 */
@Configuration
public class ComponentConfig {

    /**
     * The HTTP request.
     */
    @Autowired
    protected HttpServletRequest request;

    /**
     * The HTTP response.
     */
    @Autowired
    protected HttpServletResponse response;

    /**
     * The security config.
     */
    @Autowired(required = false)
    protected Config config;

    /**
     * Retrieve the current session store.
     *
     * @return the session store
     */
    @Bean
    @RequestScope
    public SessionStore getSessionStore() {
        Pac4jJEEConfig.configureDefaults(config);

        return config.getSessionStoreFactory().newSessionStore(request, response);
    }

    /**
     * Retrieve the current web context.
     *
     * @return the web context
     */
    @Bean
    @RequestScope
    public JEEContext getWebContext() {
        Pac4jJEEConfig.configureDefaults(config);

        return new JEEContext(request, response);
    }

    /**
     * Retrieve the current profile manager.
     *
     * @return the profile manager
     */
    @Bean
    @RequestScope
    public ProfileManager getProfileManager() {
        return config.getProfileManagerFactory().apply(getWebContext(), getSessionStore());
    }
}
