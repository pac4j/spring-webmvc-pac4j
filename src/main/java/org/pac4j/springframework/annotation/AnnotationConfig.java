package org.pac4j.springframework.annotation;

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

    @Bean
    public RequireRoleAnnotationAspect requireRoleAnnotationAspect() {
        return new RequireRoleAnnotationAspect();
    }
}
