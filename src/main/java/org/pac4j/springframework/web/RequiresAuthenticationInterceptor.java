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

import org.pac4j.core.authorization.AuthorizationChecker;
import org.pac4j.core.authorization.DefaultAuthorizationChecker;
import org.pac4j.core.client.*;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>This filter protects a resource (authentication + authorization).</p>
 * <ul>
 *  <li>If a stateful / indirect client is used, it relies on the session to get the user profile (after the {@link CallbackController} has terminated the authentication process)</li>
 *  <li>If a stateless / direct client is used, it validates the provided credentials from the request and retrieves the user profile if the authentication succeeds.</li>
 * </ul>
 * <p>Then, authorizations are checked before accessing the resource.</p>
 * <p>Forbidden or unauthorized errors can be returned. An authentication process can be started (redirection to the identity provider) in case of an indirect client.</p>
 * <p>The configuration can be provided via constructors or setters to define the {@link #config}, {@link #clientName} and {@link #authorizerName}.</p>
 *
 * @author Jerome Leleu
 * @since 1.0.0
 */
public class RequiresAuthenticationInterceptor extends HandlerInterceptorAdapter {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected ClientFinder clientFinder = new DefaultClientFinder();

    protected AuthorizationChecker authorizationChecker = new DefaultAuthorizationChecker();

    protected Config config;

    protected String clientName;

    protected String authorizerName;

    public RequiresAuthenticationInterceptor(final Config config) {
        this.config = config;
    }

    public RequiresAuthenticationInterceptor(final Config config, final String clientName) {
        this(config);
        this.clientName = clientName;
    }

    public RequiresAuthenticationInterceptor(final Config config, final String clientName, final String authorizerName) {
        this(config, clientName);
        this.authorizerName = authorizerName;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        final WebContext context = new J2EContext(request, response);
        logger.debug("url: {}", context.getFullRequestURL());

        CommonHelper.assertNotNull("config", config);
        final Clients configClients = config.getClients();
        CommonHelper.assertNotNull("configClients", configClients);
        logger.debug("clientName: {}", clientName);
        final List<Client> currentClients = clientFinder.find(configClients, context, this.clientName);
        logger.debug("currentClients: {}", currentClients);

        final boolean useSession = useSession(context, currentClients);
        logger.debug("useSession: {}", useSession);
        final ProfileManager manager = new ProfileManager(context);
        UserProfile profile = manager.get(useSession);
        logger.debug("profile: {}", profile);

        // no profile and some current clients
        if (profile == null && currentClients != null && currentClients.size() > 0) {
            // loop on all clients searching direct ones to perform authentication
            for (final Client currentClient: currentClients) {
                if (currentClient instanceof DirectClient) {
                    logger.debug("Performing authentication for client: {}", currentClient);
                    final Credentials credentials;
                    try {
                        credentials = currentClient.getCredentials(context);
                        logger.debug("credentials: {}", credentials);
                    } catch (final RequiresHttpAction e) {
                        throw new TechnicalException("Unexpected HTTP action", e);
                    }
                    profile = currentClient.getUserProfile(credentials, context);
                    logger.debug("profile: {}", profile);
                    if (profile != null) {
                        manager.save(useSession, profile);
                        break;
                    }
                }
            }
        }

        if (profile != null) {
            logger.debug("authorizerName: {}", authorizerName);
            if (authorizationChecker.isAuthorized(context, profile, authorizerName, config.getAuthorizers())) {
                logger.debug("grant access");

                return true;
            } else {
                logger.debug("forbidden");
                forbidden(context, currentClients, profile);
            }
        } else {
            if (startAuthentication(context, currentClients)) {
                logger.debug("Starting authentication");
                saveRequestedUrl(context, currentClients);
                redirectToIdentityProvider(context, currentClients);
            } else {
                logger.debug("unauthorized");
                unauthorized(context, currentClients);

            }
        }

        return false;
    }

    protected boolean useSession(final WebContext context, final List<Client> currentClients) {
        return currentClients == null || currentClients.size() == 0 || currentClients.get(0) instanceof IndirectClient;
    }

    protected void forbidden(final WebContext context, final List<Client> currentClients, final UserProfile profile) {
        context.setResponseStatus(HttpConstants.FORBIDDEN);
    }

    protected boolean startAuthentication(final WebContext context, final List<Client> currentClients) {
        return currentClients != null && currentClients.size() > 0 && currentClients.get(0) instanceof IndirectClient;
    }

    protected void saveRequestedUrl(final WebContext context, final List<Client> currentClients) {
        final String requestedUrl = context.getFullRequestURL();
        logger.debug("requestedUrl: {}", requestedUrl);
        context.setSessionAttribute(Pac4jConstants.REQUESTED_URL, requestedUrl);
    }

    protected void redirectToIdentityProvider(final WebContext context, final List<Client> currentClients) {
        try {
            final IndirectClient currentClient = (IndirectClient) currentClients.get(0);
            currentClient.redirect(context, true);
        } catch (final RequiresHttpAction e) {
            logger.debug("extra HTTP action required: {}", e.getCode());
        }
    }

    protected void unauthorized(final WebContext context, final List<Client> currentClients) {
        context.setResponseStatus(HttpConstants.UNAUTHORIZED);
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getAuthorizerName() {
        return authorizerName;
    }

    public void setAuthorizerName(String authorizerName) {
        this.authorizerName = authorizerName;
    }
}
