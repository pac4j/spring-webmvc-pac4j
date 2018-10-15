package org.pac4j.springframework.annotation;

import org.pac4j.springframework.annotation.rest.RestAnnotationAspect;
import org.pac4j.springframework.annotation.web.WebAnnotationAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * The configuration for aspects.
 *
 * @author Jerome Leleu
 * @since 3.2.0
 */
@Configuration
@EnableAspectJAutoProxy
public class AnnotationConfig {

    @Bean
    public RestAnnotationAspect restAnnotationAspect() {
        return new RestAnnotationAspect();
    }

    @Bean
    public WebAnnotationAspect webAnnotationAspect() {
        return new WebAnnotationAspect();
    }
}
