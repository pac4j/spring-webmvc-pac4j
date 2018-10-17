package org.pac4j.springframework.annotation.ws;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.pac4j.springframework.helper.WSSecurityHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The aspect to define the REST annotations.
 *
 * @author Jerome Leleu
 * @since 3.2.0
 */
@Aspect
public class WSAnnotationAspect {

    @Autowired
    private WSSecurityHelper wsSecurityHelper;

    @Before("@annotation(requireAnyRole)")
    public void beforeRequireAnyRole(final RequireAnyRole requireAnyRole) {
        wsSecurityHelper.requireAnyRole(requireAnyRole.value());
    }

    @Before("@annotation(requireAllRoles)")
    public void beforeRequireAllRoles(final RequireAllRoles requireAllRoles) {
        wsSecurityHelper.requireAllRoles(requireAllRoles.value());
    }
}
