package org.pac4j.springframework.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pac4j.core.authorization.authorizer.Authorizer;
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

import java.util.regex.Matcher;

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

    public SecurityInterceptor(final Config config) {
        this.config = config;
    }

    public SecurityInterceptor(final Config config, final String clients) {
        this(config);
        this.clients = clients;
    }

    @Deprecated
    public SecurityInterceptor(final Config config, final String clients, final HttpActionAdapter httpActionAdapter) {
        this.clients = clients;
        this.config = config;
        this.httpActionAdapter = httpActionAdapter;
    }

    public SecurityInterceptor(final Config config, final String clients, final String authorizers) {
        this(config, clients);
        this.authorizers = authorizers;
    }

    @Deprecated
    public SecurityInterceptor(final Config config, final String clients, final Authorizer[] authorizers) {
        this(config);
        SecurityEndpointBuilder.buildConfig(this, config, clients, authorizers);
    }

    public SecurityInterceptor(final Config config, final String clients, final String authorizers, final String matchers) {
        this(config, clients, authorizers);
        this.matchers = matchers;
    }

    @Deprecated
    public SecurityInterceptor(final Config config, final String clients, final Authorizer[] authorizers, final Matcher[] matchers) {
        this(config);
        SecurityEndpointBuilder.buildConfig(this, config, clients, authorizers, matchers);
    }

    public static SecurityInterceptor build(final Config config, Object... parameters) {
        final SecurityInterceptor securityInterceptor = new SecurityInterceptor(config);
        SecurityEndpointBuilder.buildConfig(securityInterceptor, config, parameters);
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

    public SecurityLogic getSecurityLogic() {
        return securityLogic;
    }

    @Override
    public void setSecurityLogic(final SecurityLogic securityLogic) {
        this.securityLogic = securityLogic;
    }

    public String getClients() {
        return clients;
    }

    @Override
    public void setClients(final String clients) {
        this.clients = clients;
    }

    public String getAuthorizers() {
        return authorizers;
    }

    @Override
    public void setAuthorizers(final String authorizers) {
        this.authorizers = authorizers;
    }

    public String getMatchers() {
        return matchers;
    }

    @Override
    public void setMatchers(final String matchers) {
        this.matchers = matchers;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(final Config config) {
        this.config = config;
    }

    public HttpActionAdapter getHttpActionAdapter() {
        return httpActionAdapter;
    }

    @Override
    public void setHttpActionAdapter(final HttpActionAdapter httpActionAdapter) {
        this.httpActionAdapter = httpActionAdapter;
    }
}
