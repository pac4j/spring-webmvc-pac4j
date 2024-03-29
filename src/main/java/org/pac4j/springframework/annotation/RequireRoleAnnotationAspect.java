package org.pac4j.springframework.annotation;

import lombok.val;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.pac4j.core.authorization.authorizer.IsAuthenticatedAuthorizer;
import org.pac4j.core.authorization.authorizer.RequireAllRolesAuthorizer;
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.exception.http.ForbiddenAction;
import org.pac4j.core.exception.http.UnauthorizedAction;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * The aspect to define the annotations.
 *
 * @author Jerome Leleu
 * @since 3.2.0
 */
@Aspect
public class RequireRoleAnnotationAspect {

    private static final IsAuthenticatedAuthorizer IS_AUTHENTICATED_AUTHORIZER = new IsAuthenticatedAuthorizer();

    @Autowired
    private JEEContext webContext;

    @Autowired
    private SessionStore sessionStore;

    @Autowired
    private ProfileManager profileManager;

    /**
     * Check if the user is authenticated and return its profiles if so.
     *
     * @return the user profiles
     */
    protected List<UserProfile> isAuthenticated() {
        val profiles = profileManager.getProfiles();

        if (!IS_AUTHENTICATED_AUTHORIZER.isAuthorized(webContext, sessionStore, profiles)) {
            throw new UnauthorizedAction();
        }
        return profiles;
    }

    /**
     * Check whether the user has one of the roles.
     *
     * @param requireAnyRole the roles config
     */
    @Before("@annotation(requireAnyRole)")
    public void beforeRequireAnyRole(final RequireAnyRole requireAnyRole) {
        val profiles = isAuthenticated();

        final RequireAnyRoleAuthorizer authorizer = new RequireAnyRoleAuthorizer(requireAnyRole.value());
        if (!authorizer.isAuthorized(webContext, sessionStore, profiles)) {
            throw new ForbiddenAction();
        }
    }

    /**
     * Check whether the user has all the roles.
     *
     * @param requireAllRoles the roles config
     */
    @Before("@annotation(requireAllRoles)")
    public void beforeRequireAllRoles(final RequireAllRoles requireAllRoles) {
        val profiles = isAuthenticated();

        val authorizer = new RequireAllRolesAuthorizer(requireAllRoles.value());
        if (!authorizer.isAuthorized(webContext, sessionStore, profiles)) {
            throw new ForbiddenAction();
        }
    }
}
