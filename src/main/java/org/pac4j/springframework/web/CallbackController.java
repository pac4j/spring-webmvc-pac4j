package org.pac4j.springframework.web;

import org.pac4j.core.config.Config;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.engine.CallbackLogic;
import org.pac4j.core.engine.DefaultCallbackLogic;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.core.http.adapter.JEEHttpActionAdapter;
import org.pac4j.core.util.FindBest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>This controller finishes the login process for an indirect client, based on the {@link #callbackLogic} and {@link #config}.</p>
 *
 * <p>The configuration can be defined via property keys: <code>pac4j.callback.defaultUrl</code> (default url after login if none was requested),
 * <code>pac4j.callback.multiProfile</code> (whether multiple profiles should be kept),
 * <code>pac4j.callback.saveInSession</code> (whether the authenticated profile should be saved in session),
 * <code>pac4j.callback.renewSession</code> (whether the session must be renewed after login) and
 * <code>pac4j.callback.path</code> (the URL path to the callback controller that will receive the redirection request).</p>
 *
 * <p>Or it can be defined via setter methods: {@link #setDefaultUrl(String)}, {@link #setMultiProfile(Boolean)}, {@link #setSaveInSession(Boolean)}
 * and ({@link #setRenewSession(Boolean)}.</p>
 *
 * @author Jerome Leleu
 * @since 1.0.0
 */
@Controller
public class CallbackController {

    private CallbackLogic<Object, JEEContext> callbackLogic;

    @Value("${pac4j.callback.defaultUrl:#{null}}")
    private String defaultUrl;

    @Value("${pac4j.callback.multiProfile:#{null}}")
    private Boolean multiProfile;

    @Value("${pac4j.callback.saveInSession:#{null}}")
    private Boolean saveInSession;

    @Value("${pac4j.callback.renewSession:#{null}}")
    private Boolean renewSession;

    @Value("${pac4j.callback.defaultClient:#{null}}")
    private String defaultClient;

    @Autowired
    private Config config;

    @RequestMapping("${pac4j.callback.path:/callback}")
    public void callback(final HttpServletRequest request, final HttpServletResponse response) {

        final SessionStore<JEEContext> bestSessionStore = FindBest.sessionStore(null, config, JEESessionStore.INSTANCE);
        final HttpActionAdapter<Object, JEEContext> bestAdapter = FindBest.httpActionAdapter(null, config, JEEHttpActionAdapter.INSTANCE);
        final CallbackLogic<Object, JEEContext> bestLogic = FindBest.callbackLogic(null, config, DefaultCallbackLogic.INSTANCE);

        final JEEContext context = new JEEContext(request, response, bestSessionStore);
        bestLogic.perform(context, config, bestAdapter, this.defaultUrl, this.saveInSession, this.multiProfile,
                this.renewSession, this.defaultClient);
    }

    @RequestMapping("${pac4j.callback.path/{cn}:/callback/{cn}}")
    public void callbackWithClientName(final HttpServletRequest request, final HttpServletResponse response, @PathVariable("cn") final String cn) {

        callback(request, response);
    }

    public String getDefaultUrl() {
        return defaultUrl;
    }

    public void setDefaultUrl(final String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

    public CallbackLogic<Object, JEEContext> getCallbackLogic() {
        return callbackLogic;
    }

    public void setCallbackLogic(final CallbackLogic<Object, JEEContext> callbackLogic) {
        this.callbackLogic = callbackLogic;
    }

    public Boolean getMultiProfile() {
        return multiProfile;
    }

    public void setMultiProfile(final Boolean multiProfile) {
        this.multiProfile = multiProfile;
    }

    public Boolean getSaveInSession() {
        return saveInSession;
    }

    public void setSaveInSession(final Boolean saveInSession) {
        this.saveInSession = saveInSession;
    }

    public Boolean getRenewSession() {
        return renewSession;
    }

    public void setRenewSession(final Boolean renewSession) {
        this.renewSession = renewSession;
    }

    public String getDefaultClient() {
        return defaultClient;
    }

    public void setDefaultClient(final String client) {
        this.defaultClient = client;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(final Config config) {
        this.config = config;
    }
}
