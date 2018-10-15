package org.pac4j.springframework.helper;

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
 * The common part of the helpers.
 *
 * @author Jerome Leleu
 * @since 3.2.0
 */
abstract class CommonSecurityHelper {

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;

    @Autowired(required = false)
    protected Config config;

    @Autowired(required = false)
    protected SessionStore<J2EContext> sessionStore;

    public SessionStore<J2EContext> getSessionStore() {
        if (sessionStore != null) {
            return sessionStore;
        } else if (config != null) {
            return config.getSessionStore();
        } else {
            return new J2ESessionStore();
        }
    }

    public J2EContext getJ2EContext() {
        return new J2EContext(request, response, getSessionStore());
    }

    public ProfileManager<CommonProfile> getProfileManager() {
        return new ProfileManager<>(getJ2EContext());
    }

    public Optional<CommonProfile> getProfile() {
        return getProfile(getDefaultReadFromSession());
    }

    public Optional<CommonProfile> getProfile(final boolean readFromSession) {
        return getProfileManager().get(readFromSession);
    }

    public List<CommonProfile> getProfiles() {
        return getProfileManager().getAll(getDefaultReadFromSession());
    }

    public List<CommonProfile> getProfiles(final boolean readFromSession) {
        return getProfileManager().getAll(readFromSession);
    }

    public List<CommonProfile> isAuthenticated() {
        return isAuthenticated(getDefaultReadFromSession());
    }

    public List<CommonProfile> isAuthenticated(final boolean readFromSession) {
        final J2EContext context = getJ2EContext();
        final List<CommonProfile> profiles = getProfiles(readFromSession);

        final IsAuthenticatedAuthorizer<CommonProfile> authorizer = new IsAuthenticatedAuthorizer<>();
        if (!authorizer.isAuthorized(context, profiles)) {
            throw HttpAction.unauthorized(getJ2EContext());
        }
        return profiles;
    }

    public void requireAnyRole(final String... roles) {
        requireAnyRole(getDefaultReadFromSession(), roles);
    }

    public void requireAnyRole(final boolean readFromSession, final String... roles) {
        final J2EContext context = getJ2EContext();
        final List<CommonProfile> profiles = isAuthenticated(readFromSession);

        final RequireAnyRoleAuthorizer<CommonProfile> authorizer = new RequireAnyRoleAuthorizer<>(roles);
        if (!authorizer.isAuthorized(context, profiles)) {
            throw HttpAction.forbidden(context);
        }
    }

    public void requireAllRoles(final String... roles) {
        requireAllRoles(getDefaultReadFromSession(), roles);
    }

    public void requireAllRoles(final boolean readFromSession, final String... roles) {
        final J2EContext context = getJ2EContext();
        final List<CommonProfile> profiles = isAuthenticated(readFromSession);

        final RequireAllRolesAuthorizer<CommonProfile> authorizer = new RequireAllRolesAuthorizer<>(roles);
        if (!authorizer.isAuthorized(context, profiles)) {
            throw HttpAction.forbidden(context);
        }
    }

    protected abstract boolean getDefaultReadFromSession();
}
