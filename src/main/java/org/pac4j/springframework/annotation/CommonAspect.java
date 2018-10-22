package org.pac4j.springframework.annotation;

import org.pac4j.core.authorization.authorizer.IsAuthenticatedAuthorizer;
import org.pac4j.core.authorization.authorizer.RequireAllRolesAuthorizer;
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Common aspect behaviors.
 *
 * @author Jerome Leleu
 * @since 3.2.0
 */
public class CommonAspect {

    private static final IsAuthenticatedAuthorizer IS_AUTHENTICATED_AUTHORIZER = new IsAuthenticatedAuthorizer();

    @Autowired
    private J2EContext webContext;

    @Autowired
    private ProfileManager profileManager;

    protected List<CommonProfile> isAuthenticated(final boolean readFromSession) {
        final List<CommonProfile> profiles = profileManager.getAll(readFromSession);

        if (!IS_AUTHENTICATED_AUTHORIZER.isAuthorized(webContext, profiles)) {
            throw HttpAction.unauthorized(webContext);
        }
        return profiles;
    }

    protected void requireAnyRole(final boolean readFromSession, final String... roles) {
        final List<CommonProfile> profiles = isAuthenticated(readFromSession);

        final RequireAnyRoleAuthorizer<CommonProfile> authorizer = new RequireAnyRoleAuthorizer<>(roles);
        if (!authorizer.isAuthorized(webContext, profiles)) {
            throw HttpAction.forbidden(webContext);
        }
    }

    protected void requireAllRoles(final boolean readFromSession, final String... roles) {
        final List<CommonProfile> profiles = isAuthenticated(readFromSession);

        final RequireAllRolesAuthorizer<CommonProfile> authorizer = new RequireAllRolesAuthorizer<>(roles);
        if (!authorizer.isAuthorized(webContext, profiles)) {
            throw HttpAction.forbidden(webContext);
        }
    }
}
