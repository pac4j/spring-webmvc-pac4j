package org.pac4j.springframework.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.engine.SecurityLogic;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.core.util.FindBest;
import org.pac4j.core.util.security.SecurityEndpoint;
import org.pac4j.core.util.security.SecurityEndpointBuilder;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.JEEContextFactory;
import org.pac4j.jee.context.session.JEESessionStoreFactory;
import org.pac4j.jee.http.adapter.JEEHttpActionAdapter;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * <p>This interceptor protects an URL.</p>
 *
 * @author Jerome Leleu
 * @since 1.0.0
 */
public class SecurityInterceptor implements HandlerInterceptor, SecurityEndpoint {

    private SecurityLogic securityLogic;

    private String clients;

    private String authorizers;

    private String matchers;

    private Config config;

    private HttpActionAdapter httpActionAdapter;

    /**
     * Empty constructor.
     */
    public SecurityInterceptor() {}

    /**
     * Constructor.
     *
     * @param config the config
     */
    public SecurityInterceptor(final Config config) {
        this.config = config;
    }

    /**
     * Constructor.
     *
     * @param config the config
     * @param clients the clients
     */
    public SecurityInterceptor(final Config config, final String clients) {
        this(config);
        this.clients = clients;
    }

    /**
     * Constructor.
     *
     * @param config the config
     * @param clients the clients
     * @param authorizers the authorizers
     */
    public SecurityInterceptor(final Config config, final String clients, final String authorizers) {
        this(config, clients);
        this.authorizers = authorizers;
    }

    /**
     * Constructor.
     *
     * @param config the config
     * @param clients the clients
     * @param authorizers the authorizers
     * @param matchers the matchers
     */
    public SecurityInterceptor(final Config config, final String clients, final String authorizers, final String matchers) {
        this(config, clients, authorizers);
        this.matchers = matchers;
    }

    /**
     * Advanced builder.
     *
     * @param parameters the parameters
     * @return the defined security interceptor
     */
    public static SecurityInterceptor build(Object... parameters) {
        final SecurityInterceptor securityInterceptor = new SecurityInterceptor();
        SecurityEndpointBuilder.buildConfig(securityInterceptor, parameters);
        return securityInterceptor;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        final HttpActionAdapter bestAdapter = FindBest.httpActionAdapter(httpActionAdapter, config, JEEHttpActionAdapter.INSTANCE);
        final SecurityLogic bestLogic = FindBest.securityLogic(securityLogic, config, DefaultSecurityLogic.INSTANCE);

        final JEEContext context = (JEEContext) FindBest.webContextFactory(null, config, JEEContextFactory.INSTANCE).newContext(request, response);
        final SessionStore sessionStore = FindBest.sessionStoreFactory(null, config, JEESessionStoreFactory.INSTANCE).newSessionStore(request, response);

        final Object result = bestLogic.perform(context, sessionStore, config, (ctx, session, profiles, parameters) -> true, bestAdapter, clients, authorizers, matchers);
        if (result == null) {
            return false;
        }
        return Boolean.parseBoolean(result.toString());
    }

    /**
     * Get the specific security logic.
     *
     * @return the security logic
     */
    public SecurityLogic getSecurityLogic() {
        return securityLogic;
    }

    /**
     * Define the specific security logic.
     *
     * @param securityLogic the security logic
     */
    @Override
    public void setSecurityLogic(final SecurityLogic securityLogic) {
        this.securityLogic = securityLogic;
    }

    /**
     * Get the clients.
     *
     * @return the clients
     */
    public String getClients() {
        return clients;
    }

    /**
     * Set the clients.
     *
     * @param clients the clients
     */
    @Override
    public void setClients(final String clients) {
        this.clients = clients;
    }

    /**
     * Get the authorizers.
     *
     * @return the authorizers
     */
    public String getAuthorizers() {
        return authorizers;
    }

    /**
     * Set the authorizers.
     *
     * @param authorizers the authorizers
     */
    @Override
    public void setAuthorizers(final String authorizers) {
        this.authorizers = authorizers;
    }

    /**
     * Get the matchers.
     *
     * @return the matchers
     */
    public String getMatchers() {
        return matchers;
    }

    /**
     * Set the matchers.
     *
     * @param matchers the matchers
     */
    @Override
    public void setMatchers(final String matchers) {
        this.matchers = matchers;
    }

    /**
     * Get the security config.
     *
     * @return the config
     */
    public Config getConfig() {
        return config;
    }

    /**
     * Define the security config.
     *
     * @param config the config
     */
    public void setConfig(final Config config) {
        this.config = config;
    }

    /**
     * Get the specific HTTP action adapter.
     *
     * @return the specific HTTP action adapter
     */
    public HttpActionAdapter getHttpActionAdapter() {
        return httpActionAdapter;
    }

    /**
     * Define the specific HTTP action adapter.
     *
     * @param httpActionAdapter the specific HTTP action adapter
     */
    @Override
    public void setHttpActionAdapter(final HttpActionAdapter httpActionAdapter) {
        this.httpActionAdapter = httpActionAdapter;
    }
}
