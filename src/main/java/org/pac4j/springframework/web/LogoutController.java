package org.pac4j.springframework.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.engine.DefaultLogoutLogic;
import org.pac4j.core.engine.LogoutLogic;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.core.util.FindBest;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.JEEContextFactory;
import org.pac4j.jee.context.session.JEESessionStoreFactory;
import org.pac4j.jee.http.adapter.JEEHttpActionAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * <p>This controller handles the (application + identity provider) logout process.</p>
 *
 * @author Jerome Leleu
 * @since 1.0.0
 */
@Controller
public class LogoutController {

    private LogoutLogic logoutLogic;

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

    /**
     * Handle the logout process.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     */
    @RequestMapping("${pac4j.logout.path:/logout}")
    public void logout(final HttpServletRequest request, final HttpServletResponse response) {

        final HttpActionAdapter bestAdapter = FindBest.httpActionAdapter(null, config, JEEHttpActionAdapter.INSTANCE);
        final LogoutLogic bestLogic = FindBest.logoutLogic(logoutLogic, config, DefaultLogoutLogic.INSTANCE);

        final JEEContext context = (JEEContext) FindBest.webContextFactory(null, config, JEEContextFactory.INSTANCE).newContext(request, response);
        final SessionStore sessionStore = FindBest.sessionStoreFactory(null, config, JEESessionStoreFactory.INSTANCE).newSessionStore(request, response);

        bestLogic.perform(context, sessionStore, config, bestAdapter, this.defaultUrl, this.logoutUrlPattern, this.localLogout, this.destroySession, this.centralLogout);
    }

    /**
     * Get the default URL.
     *
     * @return the default URL
     */
    public String getDefaultUrl() {
        return defaultUrl;
    }

    /**
     * Set the default URL.
     *
     * @param defaultUrl the default URL
     */
    public void setDefaultUrl(final String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

    /**
     * Get the logout URL pattern.
     *
     * @return the logout URL pattern
     */
    public String getLogoutUrlPattern() {
        return logoutUrlPattern;
    }

    /**
     * Set the logout URL pattern.
     *
     * @param logoutUrlPattern the logout URL pattern
     */
    public void setLogoutUrlPattern(final String logoutUrlPattern) {
        this.logoutUrlPattern = logoutUrlPattern;
    }

    /**
     * Get the specific logout logic.
     *
     * @return the logout logic
     */
    public LogoutLogic getLogoutLogic() {
        return logoutLogic;
    }

    /**
     * Set the specific logout logic.
     *
     * @param logoutLogic the logout logic
     */
    public void setLogoutLogic(final LogoutLogic logoutLogic) {
        this.logoutLogic = logoutLogic;
    }

    /**
     * Get the security config.
     *
     * @return the config
     */
    public Config getConfig() {
        return config;
    }

    /**
     * Define the security config.
     *
     * @param config the config
     */
    public void setConfig(final Config config) {
        this.config = config;
    }

    /**
     * Get whether it is a local logout.
     *
     * @return whether it is a local logout
     */
    public Boolean getLocalLogout() {
        return localLogout;
    }

    /**
     * Define whether it is a local logout.
     *
     * @param localLogout whether it is a local logout
     */
    public void setLocalLogout(final Boolean localLogout) {
        this.localLogout = localLogout;
    }

    /**
     * Get whether it is a central logout.
     *
     * @return whether it is a central logout
     */
    public Boolean getCentralLogout() {
        return centralLogout;
    }

    /**
     * Define whether it is a central logout.
     *
     * @param centralLogout whether it is a central logout
     */
    public void setCentralLogout(final Boolean centralLogout) {
        this.centralLogout = centralLogout;
    }

    /**
     * Get whether the session shoudl be destroyed at logout.
     *
     * @return whether the session shoudl be destroyed
     */
    public Boolean getDestroySession() {
        return destroySession;
    }

    /**
     * Define whether the session shoudl be destroyed at logout.
     *
     * @param destroySession whether the session shoudl be destroyed
     */
    public void setDestroySession(final Boolean destroySession) {
        this.destroySession = destroySession;
    }
}
