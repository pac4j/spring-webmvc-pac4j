package org.pac4j.springframework.controller;

import org.pac4j.core.authorization.authorizer.IsAuthenticatedAuthorizer;
import org.pac4j.core.authorization.authorizer.RequireAllRolesAuthorizer;
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.session.J2ESessionStore;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

/**
 * The parent of the abstract controllers.
 *
 * @author Jerome Leleu
 * @since 3.2.0
 */
public abstract class AbstractParentController {

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;

    @Autowired(required = false)
    protected Config config;

    @Autowired(required = false)
    protected SessionStore<J2EContext> sessionStore;

    protected SessionStore<J2EContext> getSessionStore() {
        if (sessionStore != null) {
            return sessionStore;
        } else if (config != null) {
            return config.getSessionStore();
        } else {
            return new J2ESessionStore();
        }
    }

    protected J2EContext getJ2EContext() {
        return new J2EContext(request, response, getSessionStore());
    }

    protected ProfileManager<CommonProfile> getProfileManager() {
        return new ProfileManager<>(getJ2EContext());
    }

    protected Optional<CommonProfile> getProfile() {
        return getProfile(getDefaultReadFromSession());
    }

    protected Optional<CommonProfile> getProfile(final boolean readFromSession) {
        return getProfileManager().get(readFromSession);
    }

    protected List<CommonProfile> getProfiles() {
        return getProfileManager().getAll(getDefaultReadFromSession());
    }

    protected List<CommonProfile> getProfiles(final boolean readFromSession) {
        return getProfileManager().getAll(readFromSession);
    }

    protected List<CommonProfile> isAuthenticated() {
        return isAuthenticated(getDefaultReadFromSession());
    }

    protected List<CommonProfile> isAuthenticated(final boolean readFromSession) {
        final J2EContext context = getJ2EContext();
        final List<CommonProfile> profiles = getProfiles(readFromSession);

        final IsAuthenticatedAuthorizer<CommonProfile> authorizer = new IsAuthenticatedAuthorizer<>();
        if (!authorizer.isAuthorized(context, profiles)) {
            throw HttpAction.unauthorized(getJ2EContext());
        }
        return profiles;
    }

    protected void requireAnyRole(final String... roles) {
        requireAnyRole(getDefaultReadFromSession(), roles);
    }

    protected void requireAnyRole(final boolean readFromSession, final String... roles) {
        final J2EContext context = getJ2EContext();
        final List<CommonProfile> profiles = isAuthenticated(readFromSession);

        final RequireAnyRoleAuthorizer<CommonProfile> authorizer = new RequireAnyRoleAuthorizer<>(roles);
        if (!authorizer.isAuthorized(context, profiles)) {
            throw HttpAction.forbidden(context);
        }
    }

    protected void requireAllRoles(final String... roles) {
        requireAllRoles(getDefaultReadFromSession(), roles);
    }

    protected void requireAllRoles(final boolean readFromSession, final String... roles) {
        final J2EContext context = getJ2EContext();
        final List<CommonProfile> profiles = isAuthenticated(readFromSession);

        final RequireAllRolesAuthorizer<CommonProfile> authorizer = new RequireAllRolesAuthorizer<>(roles);
        if (!authorizer.isAuthorized(context, profiles)) {
            throw HttpAction.forbidden(context);
        }
    }

    protected abstract boolean getDefaultReadFromSession();
}
