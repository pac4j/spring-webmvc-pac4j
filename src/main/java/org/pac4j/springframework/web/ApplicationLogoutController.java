/*
  Copyright 2015 - 2015 pac4j organization

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.pac4j.springframework.web;

import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.util.CommonHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Pattern;

/**
 * <p>This controller handles the application logout process.</p>
 * <p>After logout, the user is redirected to the url defined by the <i>url</i> parameter. If no url is provided, a blank page is displayed. If the url does not match the pattern, the default url is used.</p>
 * <p>The default url can be defined via the <code>pac4j.applicationLogout.defaultUrl</code> properties key.</p>
 * <p>The logout url pattern can be defined via the <code>pac4j.applicationLogout.logoutUrlPattern</code> properties key.</p>
 *
 * @author Jerome Leleu
 * @since 1.0.0
 */
@Controller
public class ApplicationLogoutController {

    @Value("${pac4j.applicationLogout.defaultUrl:}")
    protected String defaultUrl;

    @Value("${pac4j.applicationLogout.logoutUrlPattern:}")
    protected String logoutUrlPattern;

    @RequestMapping("/logout")
    public String applicationLogout(final HttpServletRequest request, final HttpServletResponse response) {

        final WebContext context = new J2EContext(request, response);
        final ProfileManager manager = new ProfileManager(context);
        manager.logout();

        final String url = context.getRequestParameter(Pac4jConstants.URL);
        if (url != null) {
            final String redirectUrl;
            if (Pattern.matches(this.logoutUrlPattern, url)) {
                redirectUrl = url;
            } else {
                redirectUrl = this.defaultUrl;
            }
            return "redirect:" + redirectUrl;
        }

        return null;
    }

    @PostConstruct
    public void postContruct() {
        if (CommonHelper.isBlank(defaultUrl)) {
            this.defaultUrl = Pac4jConstants.DEFAULT_URL_VALUE;
        }
        if (CommonHelper.isBlank(logoutUrlPattern)) {
            this.logoutUrlPattern = Pac4jConstants.DEFAULT_LOGOUT_URL_PATTERN_VALUE;
        }
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
}
