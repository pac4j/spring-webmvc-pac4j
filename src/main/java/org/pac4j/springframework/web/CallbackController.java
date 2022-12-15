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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * <p>This controller finishes the login process for an indirect client.</p>
 *
 * @author Jerome Leleu
 * @since 1.0.0
 */
@Controller
@Getter
@Setter
public class CallbackController {

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

        Pac4jJEEConfig.applyJEESettingsIfUndefined(config);

        config.getCallbackLogic().perform(config, defaultUrl, renewSession, defaultClient, new JEEFrameworkParameters(request, response));
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
}
