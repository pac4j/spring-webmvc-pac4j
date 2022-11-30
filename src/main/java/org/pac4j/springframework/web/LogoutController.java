package org.pac4j.springframework.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.pac4j.core.config.Config;
import org.pac4j.core.engine.DefaultLogoutLogic;
import org.pac4j.core.engine.LogoutLogic;
import org.pac4j.core.profile.factory.ProfileManagerFactory;
import org.pac4j.core.util.FindBest;
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
@Getter
@Setter
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

        val logic = FindBest.logoutLogic(logoutLogic, config, DefaultLogoutLogic.INSTANCE);
        val context = FindBest.webContextFactory(null, config, JEEContextFactory.INSTANCE).newContext(request, response);
        val sessionStore = FindBest.sessionStoreFactory(null, config, JEESessionStoreFactory.INSTANCE).newSessionStore(request, response);
        val profileManagerFactory = FindBest.profileManagerFactory(null, config, ProfileManagerFactory.DEFAULT);
        val adapter = FindBest.httpActionAdapter(null, config, JEEHttpActionAdapter.INSTANCE);

        logic.perform(context, sessionStore, profileManagerFactory, config, adapter, this.defaultUrl, this.logoutUrlPattern, this.localLogout, this.destroySession, this.centralLogout);
    }
}
