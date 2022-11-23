package org.pac4j.springframework.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.engine.CallbackLogic;
import org.pac4j.core.engine.DefaultCallbackLogic;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.core.util.FindBest;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.JEEContextFactory;
import org.pac4j.jee.context.session.JEESessionStoreFactory;
import org.pac4j.jee.http.adapter.JEEHttpActionAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * <p>This controller finishes the login process for an indirect client.</p>
 *
 * @author Jerome Leleu
 * @since 1.0.0
 */
@Controller
public class CallbackController {

    private CallbackLogic callbackLogic;

    @Value("${pac4j.callback.defaultUrl:#{null}}")
    private String defaultUrl;

    @Value("${pac4j.callback.renewSession:#{null}}")
    private Boolean renewSession;

    @Value("${pac4j.callback.defaultClient:#{null}}")
    private String defaultClient;

    @Autowired
    private Config config;

    /**
     * Handle the callback.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     */
    @RequestMapping("${pac4j.callback.path:/callback}")
    public void callback(final HttpServletRequest request, final HttpServletResponse response) {

        final HttpActionAdapter bestAdapter = FindBest.httpActionAdapter(null, config, JEEHttpActionAdapter.INSTANCE);
        final CallbackLogic bestLogic = FindBest.callbackLogic(callbackLogic, config, DefaultCallbackLogic.INSTANCE);

        final JEEContext context = (JEEContext) FindBest.webContextFactory(null, config, JEEContextFactory.INSTANCE).newContext(request, response);
        final SessionStore sessionStore = FindBest.sessionStoreFactory(null, config, JEESessionStoreFactory.INSTANCE).newSessionStore(request, response);

        bestLogic.perform(context, sessionStore, config, bestAdapter, this.defaultUrl, this.renewSession, this.defaultClient);
    }

    /**
     * Handle the callback (client name in path).
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param cn the client name
     */
    @RequestMapping("${pac4j.callback.path/{cn}:/callback/{cn}}")
    public void callbackWithClientName(final HttpServletRequest request, final HttpServletResponse response, @PathVariable("cn") final String cn) {

        callback(request, response);
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
     * Get the specific callback logic.
     *
     * @return the callback logic.
     */
    public CallbackLogic getCallbackLogic() {
        return callbackLogic;
    }

    /**
     * Set the specific callback logic.
     *
     * @param callbackLogic the callback logic
     */
    public void setCallbackLogic(final CallbackLogic callbackLogic) {
        this.callbackLogic = callbackLogic;
    }

    /**
     * Indicate whether the session should be renewed.
     *
     * @return whether the session should be renewed
     */
    public Boolean getRenewSession() {
        return renewSession;
    }

    /**
     * Define whether the session should be renewed.
     *
     * @param renewSession whether the session should be renewed
     */
    public void setRenewSession(final Boolean renewSession) {
        this.renewSession = renewSession;
    }

    /**
     * Get the default client.
     *
     * @return the default client
     */
    public String getDefaultClient() {
        return defaultClient;
    }

    /**
     * Define the default client (in case, none is requested on the callback URL).
     *
     * @param client the default client
     */
    public void setDefaultClient(final String client) {
        this.defaultClient = client;
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
}
