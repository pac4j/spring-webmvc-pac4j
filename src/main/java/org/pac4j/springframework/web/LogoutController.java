package org.pac4j.springframework.web;

import org.pac4j.core.config.Config;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.engine.DefaultLogoutLogic;
import org.pac4j.core.engine.LogoutLogic;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.core.http.adapter.JEEHttpActionAdapter;
import org.pac4j.core.util.FindBest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>This controller handles the (application + identity provider) logout process, based on the {@link LogoutLogic} and {@link #config}.</p>
 *
 * <p>The configuration can be provided via property keys: <code>pac4j.logout.defaultUrl</code> (default logourl url),
 * <code>pac4j.logout.logoutUrlPattern</code> (pattern that logout urls must match),
 * <code>pac4j.logout.localLogout</code> (whether the application logout must be performed)
 * <code>pac4j.logout.destroySession</code> (whether we must destroy the web session during the local logout),
 * <code>pac4j.logout.centralLogout</code> (whether the centralLogout must be performed).
 * <code>pac4j.logout.path</code> (the URL path to the logout controller).</p>
 *
 * <p>Or it can be defined via setter methods: {@link #setDefaultUrl(String)}, {@link #setLogoutUrlPattern(String)}, {@link #setLocalLogout(Boolean)},
 * {@link #setDestroySession(Boolean)} and {@link #setCentralLogout(Boolean)}.</p>
 *
 * @author Jerome Leleu
 * @since 1.0.0
 */
@Controller
public class LogoutController {

    private LogoutLogic<Object, JEEContext> logoutLogic;

    @Value("${pac4j.logout.defaultUrl:#{null}}")
    private String defaultUrl;

    @Value("${pac4j.logout.logoutUrlPattern:#{null}}")
    private String logoutUrlPattern;

    @Value("${pac4j.logout.localLogout:#{null}}")
    private Boolean localLogout;

    @Value("${pac4j.logout.destroySession:#{null}}")
    private Boolean destroySession;

    @Value("${pac4j.logout.centralLogout:#{null}}")
    private Boolean centralLogout;

    @Autowired
    private Config config;

    @RequestMapping("${pac4j.logout.path:/logout}")
    public void logout(final HttpServletRequest request, final HttpServletResponse response) {

        final SessionStore<JEEContext> bestSessionStore = FindBest.sessionStore(null, config, JEESessionStore.INSTANCE);
        final HttpActionAdapter<Object, JEEContext> bestAdapter = FindBest.httpActionAdapter(null, config, JEEHttpActionAdapter.INSTANCE);
        final LogoutLogic<Object, JEEContext> bestLogic = FindBest.logoutLogic(logoutLogic, config, DefaultLogoutLogic.INSTANCE);

        final JEEContext context = new JEEContext(request, response, bestSessionStore);
        bestLogic.perform(context, config, bestAdapter, this.defaultUrl, this.logoutUrlPattern,
                this.localLogout, this.destroySession, this.centralLogout);
    }

    public String getDefaultUrl() {
        return this.defaultUrl;
    }

    public void setDefaultUrl(final String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

    public String getLogoutUrlPattern() {
        return logoutUrlPattern;
    }

    public void setLogoutUrlPattern(final String logoutUrlPattern) {
        this.logoutUrlPattern = logoutUrlPattern;
    }

    public LogoutLogic<Object, JEEContext> getLogoutLogic() {
        return logoutLogic;
    }

    public void setLogoutLogic(final LogoutLogic<Object, JEEContext> logoutLogic) {
        this.logoutLogic = logoutLogic;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(final Config config) {
        this.config = config;
    }

    public Boolean getLocalLogout() {
        return localLogout;
    }

    public void setLocalLogout(final Boolean localLogout) {
        this.localLogout = localLogout;
    }

    public Boolean getCentralLogout() {
        return centralLogout;
    }

    public void setCentralLogout(final Boolean centralLogout) {
        this.centralLogout = centralLogout;
    }

    public Boolean getDestroySession() {
        return destroySession;
    }

    public void setDestroySession(final Boolean destroySession) {
        this.destroySession = destroySession;
    }
}
