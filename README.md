<p align="center">
  <img src="https://pac4j.github.io/pac4j/img/logo-spring-webmvc.png" width="300" />
</p>

The `spring-webmvc-pac4j` project is an **easy and powerful security library for Spring Web MVC or Spring Boot** applications which supports authentication and authorization, but also application logout and advanced features like CSRF protection. It's available under the Apache 2 license and based on the **[pac4j security engine](https://github.com/pac4j/pac4j)**.

It supports most authentication mechanisms, called [**clients**](https://github.com/pac4j/pac4j/wiki/Clients):

- **indirect / stateful clients** are for UI when the user authenticates once at an external provider (like Facebook, a CAS server...) or via a local form (or basic auth popup)  
- **direct / stateless clients** are for web services when credentials (like basic auth, tokens...) are passed for each HTTP request.

See the [authentication flows](https://github.com/pac4j/pac4j/wiki/Authentication-flows).

| The authentication mechanism you want | The `pac4j-*` submodule(s) you must use
|---------------------------------------|----------------------------------------
| OAuth (1.0 & 2.0): Facebook, Twitter, Google, Yahoo, LinkedIn, Github... | `pac4j-oauth`
| CAS (1.0, 2.0, 3.0, SAML, logout, proxy) | `pac4j-cas`
| SAML (2.0) | `pac4j-saml`
| OpenID Connect (1.0) | `pac4j-oidc`
| HTTP (form, basic auth, IP, header, cookie, GET/POST parameter)<br />+<br />JWT<br />or LDAP<br />or Relational DB<br />or MongoDB<br />or Stormpath<br />or CAS REST API| `pac4j-http`<br />+<br />`pac4j-jwt`<br />or `pac4j-ldap`<br />or `pac4j-sql`<br />or `pac4j-mongo`<br />or `pac4j-stormpath`<br />or `pac4j-cas`
| Google App Engine UserService | `pac4j-gae`
| OpenID | `pac4j-openid`

It also supports many authorization checks, called [**authorizers**](https://github.com/pac4j/pac4j/wiki/Authorizers) available in the `pac4j-core` (and `pac4j-http`) submodules: role / permission checks, IP check, profile type verification, HTTP method verification... as well as regular security protections for CSRF, XSS, cache control, Xframe...


## How to use it?

First, you need to add a dependency on this library as well as on the appropriate `pac4j` submodules. Then, you must define the [**clients**](https://github.com/pac4j/pac4j/wiki/Clients) for authentication and the [**authorizers**](https://github.com/pac4j/pac4j/wiki/Authorizers) to check authorizations.

Define the `CallbackController` to finish authentication processes if you use indirect clients (like Facebook).

Use the `RequiresAuthenticationInterceptor` to secure the urls of your web application (using the `clientName` parameter for authentication and the `authorizerName` parameter for authorizations).

Just follow these easy steps:


### Add the required dependencies (`spring-webmvc-pac4j` + `pac4j-*` libraries)

You need to add a dependency on the `spring-webmvc-pac4j` library (<em>groupId</em>: **org.pac4j**, *version*: **1.0.1**) as well as on the appropriate `pac4j` submodules (<em>groupId</em>: **org.pac4j**, *version*: **1.8.1**): the `pac4j-oauth` dependency for OAuth support, the `pac4j-cas` dependency for CAS support, the `pac4j-ldap` module for LDAP authentication, ...

All artifacts are available in the [Maven central repository](http://search.maven.org/#search%7Cga%7C1%7Cpac4j).


### Define the configuration (`Config` + `Clients` + `XXXClient` + `Authorizer`)

Each authentication mechanism (Facebook, Twitter, a CAS server...) is defined by a client (implementing the `org.pac4j.core.client.Client` interface). All clients must be gathered in a `org.pac4j.core.client.Clients` class.

All `Clients` must be defined in a `org.pac4j.core.config.Config` object as well as the authorizers which will be used by the application. This can be setup either by a Spring context file or by a Spring configuration class.

#### Spring context file:

``` xml
<bean id="oidClient" class="org.pac4j.oidc.client.OidcClient">
    <property name="clientID" value="id" />
    <property name="secret" value="secret" />
    <property name="discoveryURI" value="https://accounts.google.com/.well-known/openid-configuration" />
    <property name="customParams">
        <map>
            <entry key="prompt" value="consent" />
        </map>
    </property>
</bean>
<bean id="samlConfig" class="org.pac4j.saml.client.SAML2ClientConfiguration">
    <constructor-arg name="keystorePath" value="resource:samlKeystore.jks" />
    <constructor-arg name="keystorePassword" value="pac4j-demo-passwd" />
    <constructor-arg name="privateKeyPassword" value="pac4j-demo-passwd" />
    <constructor-arg name="identityProviderMetadataPath" value="resource:testshib-providers.xml" />
    <property name="maximumAuthenticationLifetime" value="3600" />
    <property name="serviceProviderEntityId" value="urn:mace:saml:pac4j.org" />
    <property name="serviceProviderMetadataPath" value="sp-metadata.xml" />
</bean>
<bean id="saml2Client" class="org.pac4j.saml.client.SAML2Client">
    <constructor-arg name="configuration" ref="samlConfig" />
</bean>
<bean id="facebookClient" class="org.pac4j.oauth.client.FacebookClient">
    <constructor-arg name="key" value="fbId" />
    <constructor-arg name="secret" value="fbSecret" />
</bean>
<bean id="twitterClient" class="org.pac4j.oauth.client.TwitterClient">
    <constructor-arg name="key" value="twId" />
    <constructor-arg name="secret" value="twSecret" />
</bean>
<bean id="testAuthenticator" class="org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator">
</bean>
<bean id="formClient" class="org.pac4j.http.client.indirect.FormClient">
    <constructor-arg name="loginUrl" value="http://localhost:8080/theForm" />
    <constructor-arg name="usernamePasswordAuthenticator" ref="testAuthenticator" />
</bean>
<bean id="indirectBasicAuthClient" class="org.pac4j.http.client.indirect.IndirectBasicAuthClient">
    <constructor-arg name="usernamePasswordAuthenticator" ref="testAuthenticator" />
</bean>
<bean id="casClient" class="org.pac4j.cas.client.CasClient">
    <property name="casLoginUrl" value="http://mycasserver/login" />
</bean>
<bean id="parameterClient" class="org.pac4j.http.client.direct.ParameterClient">
    <constructor-arg name="parameterName" value="token" />
    <constructor-arg name="tokenAuthenticator">
        <bean class="org.pac4j.jwt.credentials.authenticator.JwtAuthenticator">
            <constructor-arg name="secret" value="secret" />
        </bean>
    </constructor-arg>
    <property name="supportGetRequest" value="true" />
    <property name="supportPostRequest" value="false" />
</bean>
<bean id="clients" class="org.pac4j.core.client.Clients">
    <constructor-arg name="callbackUrl" value="http://localhost:8080/callback" />
    <constructor-arg name="clients">
        <list>
            <ref bean="oidClient" />
            <ref bean="saml2Client" />
            <ref bean="facebookClient" />
            <ref bean="twitterClient" />
            <ref bean="formClient" />
            <ref bean="indirectBasicAuthClient" />
            <ref bean="casClient" />
            <ref bean="parameterClient" />
        </list>
    </constructor-arg>
</bean>
<bean id="adminRoleAuthorizer" class="org.pac4j.core.authorization.RequireAnyRoleAuthorizer">
    <constructor-arg name="roles" value="ROLE_ADMIN" />
</bean>
<bean id="customAuthorizer" class="org.pac4j.demo.spring.authorizer.CustomAuthorizer" />
<bean id="config" class="org.pac4j.core.config.Config">
    <constructor-arg name="clients" ref="clients" />
    <constructor-arg name="authorizers">
        <map>
            <entry key="admin" value-ref="adminRoleAuthorizer" />
            <entry key="custom" value-ref="customAuthorizer" />
        </map>
    </constructor-arg>
</bean>
```

#### Spring configuration class:

``` java
@Configuration
public class Pac4jConfig {

    @Bean
    public Config config() {
        final OidcClient oidcClient = new OidcClient();
        oidcClient.setClientID("id");
        oidcClient.setSecret("secret");
        oidcClient.setDiscoveryURI("https://accounts.google.com/.well-known/openid-configuration");
        oidcClient.addCustomParam("prompt", "consent");

        final SAML2ClientConfiguration cfg = new SAML2ClientConfiguration("resource:samlKeystore.jks", "pac4j-demo-passwd", "pac4j-demo-passwd", "resource:testshib-providers.xml");
        cfg.setMaximumAuthenticationLifetime(3600);
        cfg.setServiceProviderEntityId("urn:mace:saml:pac4j.org");
        cfg.setServiceProviderMetadataPath(new File("target", "sp-metadata.xml").getAbsolutePath());
        final SAML2Client saml2Client = new SAML2Client(cfg);

        final FacebookClient facebookClient = new FacebookClient("fbId", "fbSecret");
        final TwitterClient twitterClient = new TwitterClient("twId", "twSecret");

        final FormClient formClient = new FormClient("http://localhost:8080/theForm.jsp", new SimpleTestUsernamePasswordAuthenticator());
        final IndirectBasicAuthClient indirectBasicAuthClient = new IndirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator());

        final CasClient casClient = new CasClient();
        casClient.setCasLoginUrl("http://mycasserver/login");

        ParameterClient parameterClient = new ParameterClient("token", new JwtAuthenticator("secret"));
        parameterClient.setSupportGetRequest(true);
        parameterClient.setSupportPostRequest(false);

        final Clients clients = new Clients("http://localhost:8080/callback", oidcClient, saml2Client, facebookClient,
                twitterClient, formClient, indirectBasicAuthClient, casClient, parameterClient);

        final Config config = new Config(clients);
        config.addAuthorizer("admin", new RequireAnyRoleAuthorizer("ROLE_ADMIN"));
        config.addAuthorizer("custom", new CustomAuthorizer());
        return config;
    }
}
```

"http://localhost:8080/callback" is the url of the callback endpoint (see below). It may not be defined for REST support / direct clients only.


### Define the callback endpoint (only for stateful / indirect authentication mechanisms)

Indirect clients rely on external identity providers (like Facebook) and thus require to define a callback endpoint in the application where the user will be redirected after login at the identity provider. For REST support / direct clients only, this callback endpoint is not necessary.  
It must be defined by scanning the `org.pac4j.springframework.web.CallbackController` class.

#### Spring context file:

``` xml
<context:component-scan base-package="org.pac4j.springframework.web" />
```

#### Spring configuration class:

``` java
@ComponentScan(basePackages = "org.pac4j.springframework.web")
```

This controller will be available on the `/callback` url. The default url where the user will be redirected after login if no url was originally requested can be specified by the `pac4j.callback.defaultUrl` properties key (by default: `/`).


### Protect an url (authentication + authorization)

You can protect an url and require the user to be authenticated by a client (and optionally have the appropriate authorizations) by using the `RequiresAuthenticationInterceptor`:

#### Spring context file:

``` xml
<mvc:interceptors>
    <mvc:interceptor>
        <mvc:mapping path="/facebookadmin/*" />
        <bean class="org.pac4j.springframework.web.RequiresAuthenticationInterceptor">
            <constructor-arg name="config" ref="config" />
            <constructor-arg name="clientName" value="FacebookClient" />
            <constructor-arg name="authorizerName" value="admin" />
        </bean>
    </mvc:interceptor>
    ...
```

#### Spring configuration class:

``` java
@Configuration
@ComponentScan(basePackages = "org.pac4j.springframework.web")
public class SecurityConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private Config config;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequiresAuthenticationInterceptor(config, "FacebookClient", "admin")).addPathPatterns("/facebookadmin/*");
        ...
    }
}

```

The following constructor parameters are available:

- `config`: this is the `Config` already defined with all the clients and authorizers
- `clientName` (optional): the list of client names (separated by commas) used for authentication. If the user is not authenticated, direct clients are tried successively then if the user is still not authenticated and if the first client is an indirect one, this client is used to start the authentication. Otherwise, a 401 HTTP error is returned. If the *client_name* request parameter is provided, only the matching client is selected
- `authorizerName` (optional): the list of authorizer names (separated by commas) used to check authorizations. If the user is not authorized, a 403 HTTP error is returned. By default (if blank), the user only requires to be authenticated to access the resource. The following authorizers are available by default:
  * `hsts` to use the `StrictTransportSecurityHeader` authorizer, `nosniff` for `XContentTypeOptionsHeader`, `noframe` for `XFrameOptionsHeader `, `xssprotection` for `XSSProtectionHeader `, `nocache` for `CacheControlHeader ` or `securityHeaders` for the five previous authorizers
  * `csrfToken` to use the `CsrfTokenGeneratorAuthorizer` with the `DefaultCsrfTokenGenerator` (it generates a CSRF token and adds it to the request and save it in the `pac4jCsrfToken` cookie), `csrfCheck` to check that this previous token has been sent as the `pac4jCsrfToken` header or parameter in a POST request and `csrf` to use both previous authorizers.


### Get redirection urls

You can also explicitly compute a redirection url to a provider by using the `getRedirectAction` method of the client, in order to create an explicit link for login. For example with Facebook:

	FacebookClient fbClient = (FacebookClient) client.findClient("FacebookClient");
	WebContext context = new J2EContext(request, response);
	String fbLoginUrl = fbClient.getRedirectAction(context, false).getLocation();


### Get the user profile

You can test if the user is authenticated using the `ProfileManager.isAuthenticated()` method or get the user profile using the `ProfileManager.get(true)` method (`false` not to use the session, but only the current HTTP request).

The retrieved profile is at least a `CommonProfile`, from which you can retrieve the most common properties that all profiles share. But you can also cast the user profile to the appropriate profile according to the provider used for authentication. For example, after a Facebook authentication:
 
    FacebookProfile facebookProfile = (FacebookProfile) commonProfile;


### Logout

You can log out the current authenticated user using the `org.pac4j.springframework.web.ApplicationLogoutController` (defined by classpath scanning, like for the `CallbackController`).

To perfom the logout, you must call the /logout url. A blank page is displayed by default unless an *url* request parameter is provided. In that case, the user will be redirected to this specified url (if it matches the logout url pattern defined) or to the default logout url otherwise.

The following properties key can be defined:

- `pac4j.applicationLogout.defaultUrl` (optional): the default logout url if the provided *url* parameter does not match the logout url pattern (by default: /)
- `pac4j.applicationLogout.logoutUrlPattern` (optional): the logout url pattern that the logout url must match (it's a security check, only relative urls are allowed by default).


## Demos

The demo webapp for Spring Web MVC: [spring-webmvc-pac4j-demo](https://github.com/pac4j/spring-webmvc-pac4j-demo) or for Spring Boot: [spring-webmvc-pac4j-boot-demo](https://github.com/pac4j/spring-webmvc-pac4j-boot-demo) are available for tests and implement many authentication mechanisms: Facebook, Twitter, form, basic auth, CAS, SAML, OpenID Connect, JWT...


## Release notes

See the [release notes](https://github.com/pac4j/spring-webmvc-pac4j/wiki/Release-Notes). Learn more by browsing the [spring-webmvc-pac4j Javadoc](http://www.javadoc.io/doc/org.pac4j/spring-webmvc-pac4j/1.0.1) and the [pac4j Javadoc](http://www.pac4j.org/apidocs/pac4j/1.8.1/index.html).


## Need help?

If you have any question, please use the following mailing lists:

- [pac4j users](https://groups.google.com/forum/?hl=en#!forum/pac4j-users)
- [pac4j developers](https://groups.google.com/forum/?hl=en#!forum/pac4j-dev)

## Development

The next version 1.0.2-SNAPSHOT is under development.

Maven artifacts are built via Travis: [![Build Status](https://travis-ci.org/pac4j/spring-webmvc-pac4j.png?branch=master)](https://travis-ci.org/pac4j/spring-webmvc-pac4j) and available in the [Sonatype snapshots repository](https://oss.sonatype.org/content/repositories/snapshots/org/pac4j). This repository must be added in the Maven *pom.xml* file for example:

    <repositories>
      <repository>
        <id>sonatype-nexus-snapshots</id>
        <name>Sonatype Nexus Snapshots</name>
        <url>https://oss.sonatype.org/content/repositories/snapshots</url>
        <releases>
          <enabled>false</enabled>
        </releases>
        <snapshots>
          <enabled>true</enabled>
        </snapshots>
      </repository>
    </repositories>
