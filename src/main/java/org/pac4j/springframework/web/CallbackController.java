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

import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>This controller handles the callback from the identity provider to finish the authentication process.</p>
 * <p>The default url after login (if none has originally be requested) can be defined via the <code>pac4j.callback.defaultUrl</code> properties key.</p>
 *
 * @author Jerome Leleu
 * @since 1.0.0
 */
@Controller
public class CallbackController {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${pac4j.callback.defaultUrl:}")
    protected String defaultUrl;

    @Autowired
    protected Config config;

    @RequestMapping("/callback")
    public String callback(final HttpServletRequest request, final HttpServletResponse response) {

        final WebContext context = new J2EContext(request, response);

        CommonHelper.assertNotNull("config", config);
        final Clients clients = config.getClients();
        CommonHelper.assertNotNull("clients", clients);
        final Client client = clients.findClient(context);
        logger.debug("client: {}", client);
        CommonHelper.assertNotNull("client", client);
        CommonHelper.assertTrue(client instanceof IndirectClient, "only indirect clients are allowed on the callback url");

        final Credentials credentials;
        try {
            credentials = client.getCredentials(context);
        } catch (final RequiresHttpAction e) {
            logger.debug("extra HTTP action required: {}", e.getCode());
            return null;
        }
        logger.debug("credentials: {}", credentials);

        final UserProfile profile = client.getUserProfile(credentials, context);
        logger.debug("profile: {}", profile);
        saveUserProfile(context, profile);
        return redirectToOriginallyRequestedUrl(context);
    }

    @PostConstruct
    public void postContruct() {
        if (CommonHelper.isBlank(defaultUrl)) {
            this.defaultUrl = Pac4jConstants.DEFAULT_URL_VALUE;
        }
    }
    protected void saveUserProfile(final WebContext context, final UserProfile profile) {
        final ProfileManager manager = new ProfileManager(context);
        if (profile != null) {
            manager.save(true, profile);
        }
    }

    protected String redirectToOriginallyRequestedUrl(final WebContext context) {
        final String requestedUrl = (String) context.getSessionAttribute(Pac4jConstants.REQUESTED_URL);
        logger.debug("requestedUrl: {}", requestedUrl);
        final String redirectUrl;
        if (CommonHelper.isNotBlank(requestedUrl)) {
            context.setSessionAttribute(Pac4jConstants.REQUESTED_URL, null);
            redirectUrl = requestedUrl;
        } else {
            redirectUrl = this.defaultUrl;
        }
        return "redirect:" + redirectUrl;
    }

    public String getDefaultUrl() {
        return defaultUrl;
    }

    public void setDefaultUrl(String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }
}
