package org.pac4j.springframework.component;

import org.pac4j.core.config.Config;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.session.J2ESessionStore;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.ProfileManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The configuration of the pac4j components.
 *
 * @author Jerome Leleu
 * @since 3.2.0
 */
@Configuration
public class ComponentConfig {

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;

    @Autowired(required = false)
    protected Config config;

    @Autowired(required = false)
    protected SessionStore<J2EContext> sessionStore;

    protected SessionStore<J2EContext> getSessionStore() {
        if (sessionStore != null) {
            return sessionStore;
        } else if (config != null) {
            return config.getSessionStore();
        } else {
            return new J2ESessionStore();
        }
    }

    @Bean
    @RequestScope
    public J2EContext getWebContext() {
        return new J2EContext(request, response, getSessionStore());
    }

    @Bean
    @RequestScope
    public ProfileManager getProfileManager() {
        return new ProfileManager<>(getWebContext());
    }
}
