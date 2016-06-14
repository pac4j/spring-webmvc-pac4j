package org.pac4j.springframework.web;

import org.pac4j.core.config.Config;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.engine.SecurityLogic;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.pac4j.core.util.CommonHelper.assertNotNull;

/**
 * <p>This filter protects an url, based on the {@link #securityLogic}.</p>
 *
 * <p>The configuration can be provided via contructors or setter methods: {@link #setConfig(Config)} (the security configuration),
 * {@link #setClients(String)} (list of clients for authentication), {@link #setAuthorizers(String)} (list of authorizers),
 * {@link #setMatchers(String)} (list of matchers) and {@link #setMultiProfile(Boolean)} (whether multiple profiles should be kept).</p>
 *
 * @author Jerome Leleu
 * @since 1.0.0
 */
public class SecurityInterceptor extends HandlerInterceptorAdapter {

    private SecurityLogic<Boolean, J2EContext> securityLogic = new DefaultSecurityLogic<>();

    private String clients;

    private String authorizers;

    private String matchers;

    private Boolean multiProfile;

    private Config config;

    public SecurityInterceptor(final Config config) {
        this.config = config;
    }

    public SecurityInterceptor(final Config config, final String clients) {
        this(config);
        this.clients = clients;
    }

    public SecurityInterceptor(final Config config, final String clients, final String authorizers) {
        this(config, clients);
        this.authorizers = authorizers;
    }

    public SecurityInterceptor(final Config config, final String clients, final String authorizers, final String matchers) {
        this(config, clients, authorizers);
        this.matchers = matchers;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        assertNotNull("config", config);
        final J2EContext context = new J2EContext(request, response, config.getSessionStore());

        return securityLogic.perform(context, config, (ctx, parameters) -> true, (code, webCtx) -> false, clients, authorizers, matchers, multiProfile);
    }

    public SecurityLogic<Boolean, J2EContext> getSecurityLogic() {
        return securityLogic;
    }

    public void setSecurityLogic(SecurityLogic<Boolean, J2EContext> securityLogic) {
        this.securityLogic = securityLogic;
    }

    public String getClients() {
        return clients;
    }

    public void setClients(String clients) {
        this.clients = clients;
    }

    public String getAuthorizers() {
        return authorizers;
    }

    public void setAuthorizers(String authorizers) {
        this.authorizers = authorizers;
    }

    public String getMatchers() {
        return matchers;
    }

    public void setMatchers(String matchers) {
        this.matchers = matchers;
    }

    public Boolean getMultiProfile() {
        return multiProfile;
    }

    public void setMultiProfile(Boolean multiProfile) {
        this.multiProfile = multiProfile;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }
}
