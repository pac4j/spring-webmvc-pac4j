package org.pac4j.springframework.annotation;

import org.pac4j.core.config.Config;
import org.pac4j.springframework.web.DefaultHttpActionExceptionResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * The configuration for the aspects.
 *
 * @author Jerome Leleu
 * @since 3.2.0
 */
@Configuration
@EnableAspectJAutoProxy
public class AnnotationConfig {

    /**
     * Role aspect configuration.
     *
     * @return the aspect
     */
    @Bean
    public RequireRoleAnnotationAspect requireRoleAnnotationAspect() {
        return new RequireRoleAnnotationAspect();
    }

    /**
     * The exception resolver which turns the HTTP actions thrown by the annotations into HTTP responses.
     *
     * @param config the config
     * @return the exception resolver
     */
    @Bean
    public DefaultHttpActionExceptionResolver defaultHttpActionExceptionResolver(final Config config) {
        return new DefaultHttpActionExceptionResolver(config);
    }
}
