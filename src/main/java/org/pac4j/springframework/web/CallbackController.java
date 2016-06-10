package org.pac4j.springframework.web;

import org.pac4j.core.config.Config;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.engine.CallbackLogic;
import org.pac4j.core.engine.J2ERenewSessionCallbackLogic;
import org.pac4j.core.http.J2ENopHttpActionAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.pac4j.core.util.CommonHelper.assertNotNull;

/**
 * <p>This filter finishes the login process for an indirect client, based on the {@link #callbackLogic}.</p>
 *
 * <p>The configuration can be defined via property keys: <code>pac4j.callback.defaultUrl</code> (default url after login if none was requested),
 * <code>pac4j.callback.multiProfile</code> (whether multiple profiles should be kept) and
 * <code>pac4j.callback.renewSession</code> (whether the session must be renewed after login).</p>
 * <p>Or it can be defined via setter methods: {@link #setDefaultUrl(String)}, {@link #setMultiProfile(Boolean)} and ({@link #setRenewSession(Boolean)}.</p>
 *
 * @author Jerome Leleu
 * @since 1.0.0
 */
@Controller
public class CallbackController {

    private CallbackLogic<Object, J2EContext> callbackLogic = new J2ERenewSessionCallbackLogic();

    @Value("${pac4j.callback.defaultUrl:#{null}}")
    private String defaultUrl;

    @Value("${pac4j.callback.multiProfile:#{null}}")
    private Boolean multiProfile;

    @Value("${pac4j.callback.renewSession:#{null}}")
    private Boolean renewSession;

    @Autowired
    private Config config;

    @RequestMapping("/callback")
    public String callback(final HttpServletRequest request, final HttpServletResponse response) {

        assertNotNull("config", config);
        final J2EContext context = new J2EContext(request, response, config.getSessionStore());

        callbackLogic.perform(context, config, J2ENopHttpActionAdapter.INSTANCE, this.defaultUrl, this.multiProfile, this.renewSession);

        return null;
    }

    public String getDefaultUrl() {
        return defaultUrl;
    }

    public void setDefaultUrl(String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

    public CallbackLogic<Object, J2EContext> getCallbackLogic() {
        return callbackLogic;
    }

    public void setCallbackLogic(CallbackLogic<Object, J2EContext> callbackLogic) {
        this.callbackLogic = callbackLogic;
    }

    public Boolean getMultiProfile() {
        return multiProfile;
    }

    public void setMultiProfile(Boolean multiProfile) {
        this.multiProfile = multiProfile;
    }

    public Boolean getRenewSession() {
        return renewSession;
    }

    public void setRenewSession(Boolean renewSession) {
        this.renewSession = renewSession;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }
}
