package org.pac4j.springframework.annotation.ui;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.pac4j.springframework.helper.UISecurityHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The aspect to define the web annotations.
 *
 * @author Jerome Leleu
 * @since 3.2.0
 */
@Aspect
public class UIAnnotationAspect {

    @Autowired
    private UISecurityHelper uiSecurityHelper;

    @Before("@annotation(requireAnyRole)")
    public void beforeRequireAnyRole(final RequireAnyRole requireAnyRole) {
        uiSecurityHelper.requireAnyRole(requireAnyRole.value());
    }

    @Before("@annotation(requireAllRoles)")
    public void beforeRequireAllRoles(final RequireAllRoles requireAllRoles) {
        uiSecurityHelper.requireAllRoles(requireAllRoles.value());
    }
}
