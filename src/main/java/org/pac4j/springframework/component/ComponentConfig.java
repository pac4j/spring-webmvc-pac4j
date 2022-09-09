package org.pac4j.springframework.component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.util.FindBest;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
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

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;

    @Autowired(required = false)
    protected Config config;

    @Bean
    @RequestScope
    public SessionStore getSessionStore() {
        return FindBest.sessionStore(null, config, JEESessionStore.INSTANCE);
    }

    @Bean
    @RequestScope
    public JEEContext getWebContext() {
        return new JEEContext(request, response);
    }

    @Bean
    @RequestScope
    public ProfileManager getProfileManager() {
        return new ProfileManager(getWebContext(), getSessionStore());
    }
}
