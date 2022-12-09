package org.pac4j.springframework.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.pac4j.core.config.Config;
import org.pac4j.core.util.security.SecurityEndpoint;
import org.pac4j.core.util.security.SecurityEndpointBuilder;
import org.pac4j.jee.config.Pac4jJEEConfig;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * <p>This interceptor protects an URL.</p>
 *
 * @author Jerome Leleu
 * @since 1.0.0
 */
@Getter
@Setter
public class SecurityInterceptor implements HandlerInterceptor, SecurityEndpoint {

    private String clients;

    private String authorizers;

    private String matchers;

    private Config config;

    /**
     * Empty constructor.
     */
    public SecurityInterceptor() {
    }

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
     * @param config  the config
     * @param clients the clients
     */
    public SecurityInterceptor(final Config config, final String clients) {
        this(config);
        this.clients = clients;
    }

    /**
     * Constructor.
     *
     * @param config      the config
     * @param clients     the clients
     * @param authorizers the authorizers
     */
    public SecurityInterceptor(final Config config, final String clients, final String authorizers) {
        this(config, clients);
        this.authorizers = authorizers;
    }

    /**
     * Constructor.
     *
     * @param config      the config
     * @param clients     the clients
     * @param authorizers the authorizers
     * @param matchers    the matchers
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
        val securityInterceptor = new SecurityInterceptor();
        SecurityEndpointBuilder.buildConfig(securityInterceptor, parameters);
        return securityInterceptor;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        Pac4jJEEConfig.configureDefaults(config);

        val result = config.getSecurityLogic().perform(config, (ctx, session, profiles, parameters) -> true,
                clients, authorizers, matchers, request, response);
        if (result == null) {
            return false;
        }
        return Boolean.parseBoolean(result.toString());
    }
}
