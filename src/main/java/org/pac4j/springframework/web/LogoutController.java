package org.pac4j.springframework.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import org.pac4j.core.config.Config;
import org.pac4j.jee.config.Pac4jJEEConfig;
import org.pac4j.jee.context.JEEFrameworkParameters;
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

        Pac4jJEEConfig.applyJEESettingsIfUndefined(config);

        config.getLogoutLogic().perform(config, defaultUrl, logoutUrlPattern, localLogout, destroySession, centralLogout, new JEEFrameworkParameters(request, response));
    }
}
