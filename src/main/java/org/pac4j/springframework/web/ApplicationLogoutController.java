package org.pac4j.springframework.web;

import org.pac4j.core.config.Config;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.engine.ApplicationLogoutLogic;
import org.pac4j.core.engine.DefaultApplicationLogoutLogic;
import org.pac4j.core.http.J2ENopHttpActionAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.pac4j.core.util.CommonHelper.assertNotNull;

/**
 * <p>This filter handles the application logout process, based on the {@link #applicationLogoutLogic}.</p>
 *
 * <p>The configuration can be defined via property keys: <code>pac4j.applicationLogout.defaultUrl</code> (default logourl url) and
 * <code>pac4j.applicationLogout.logoutUrlPattern</code> (pattern that logout urls must match).</p>
 * <p>Or it can be defined via setter methods: {@link #setDefaultUrl(String)} and {@link #setLogoutUrlPattern(String)}.</p>
 *
 * @author Jerome Leleu
 * @since 1.0.0
 */
@Controller
public class ApplicationLogoutController {

    private ApplicationLogoutLogic<Object, J2EContext> applicationLogoutLogic = new DefaultApplicationLogoutLogic<>();

    @Value("${pac4j.applicationLogout.defaultUrl:#{null}}")
    private String defaultUrl;

    @Value("${pac4j.applicationLogout.logoutUrlPattern:#{null}}")
    private String logoutUrlPattern;

    @Autowired
    private Config config;

    @RequestMapping("/logout")
    public void applicationLogout(final HttpServletRequest request, final HttpServletResponse response) {

        assertNotNull("applicationLogoutLogic", applicationLogoutLogic);
        assertNotNull("config", config);
        final J2EContext context = new J2EContext(request, response, config.getSessionStore());

        applicationLogoutLogic.perform(context, config, J2ENopHttpActionAdapter.INSTANCE, this.defaultUrl, this.logoutUrlPattern);
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

    public void setLogoutUrlPattern(String logoutUrlPattern) {
        this.logoutUrlPattern = logoutUrlPattern;
    }

    public ApplicationLogoutLogic<Object, J2EContext> getApplicationLogoutLogic() {
        return applicationLogoutLogic;
    }

    public void setApplicationLogoutLogic(ApplicationLogoutLogic<Object, J2EContext> applicationLogoutLogic) {
        this.applicationLogoutLogic = applicationLogoutLogic;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }
}
