package org.pac4j.springframework.config;

import org.pac4j.core.config.Config;
import org.pac4j.springframework.annotation.AnnotationConfig;
import org.pac4j.springframework.component.ComponentConfig;
import org.pac4j.springframework.web.SecurityInterceptor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * To simplify security configuration.
 *
 * @author Jerome LELEU
 * @since 8.0.2
 */
// for default pac4j objects and annotations to be available:
@Import({ComponentConfig.class, AnnotationConfig.class})
// to register the logout and callback controllers:
@ComponentScan(basePackages = "org.pac4j.springframework.web")
public abstract class Pac4jSecurityConfig implements WebMvcConfigurer {

    /**
     * Build the pac4j configuration.
     *
     * @return the configuration
     */
    public abstract Config config();

    /**
     * Add as security interceptor.
     *
     * @param registry the interceptor registry
     * @param parameters the built parameters
     * @return the registered interceptor
     */
    protected InterceptorRegistration addSecurity(final InterceptorRegistry registry, final Object... parameters) {
        return registry.addInterceptor(SecurityInterceptor.build(config(), parameters));
    }

    /**
     * Add as security interceptor with a dedicated configuration.
     *
     * @param registry the interceptor registry
     * @param newConfig the new configuration
     * @param parameters the built parameters
     * @return the registered interceptor
     */
    protected InterceptorRegistration addSecurityWithConfig(final InterceptorRegistry registry,
                                                            final Config newConfig, final Object... parameters) {
        return registry.addInterceptor(SecurityInterceptor.build(newConfig, parameters));
    }
}
