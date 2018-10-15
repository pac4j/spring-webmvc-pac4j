package org.pac4j.springframework.annotation.web;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.pac4j.springframework.helper.WebSecurityHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The aspect to define the web annotations.
 *
 * @author Jerome Leleu
 * @since 3.2.0
 */
@Aspect
public class WebAnnotationAspect {

    @Autowired
    private WebSecurityHelper webSecurityHelper;

    @Before("@annotation(requireAnyRole)")
    public void beforeRequireAnyRole(final RequireAnyRole requireAnyRole) {
        webSecurityHelper.requireAnyRole(requireAnyRole.value());
    }

    @Before("@annotation(requireAllRoles)")
    public void beforeRequireAllRoles(final RequireAllRoles requireAllRoles) {
        webSecurityHelper.requireAllRoles(requireAllRoles.value());
    }
}
