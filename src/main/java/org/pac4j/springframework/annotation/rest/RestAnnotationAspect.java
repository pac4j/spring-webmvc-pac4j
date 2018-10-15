package org.pac4j.springframework.annotation.rest;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.pac4j.springframework.helper.RestSecurityHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The aspect to define the REST annotations.
 *
 * @author Jerome Leleu
 * @since 3.2.0
 */
@Aspect
public class RestAnnotationAspect {

    @Autowired
    private RestSecurityHelper restSecurityHelper;

    @Before("@annotation(requireAnyRole)")
    public void beforeRequireAnyRole(final RequireAnyRole requireAnyRole) {
        restSecurityHelper.requireAnyRole(requireAnyRole.value());
    }

    @Before("@annotation(requireAllRoles)")
    public void beforeRequireAllRoles(final RequireAllRoles requireAllRoles) {
        restSecurityHelper.requireAllRoles(requireAllRoles.value());
    }
}
