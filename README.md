<p align="center">
  <img src="https://pac4j.github.io/pac4j/img/logo-spring-webmvc.png" width="300" />
</p>

The `spring-webmvc-pac4j` project is an **easy and powerful security library for Spring Web MVC** (with or without Spring Boot) web applications. It supports authentication and authorization, but also application logout and advanced features like session fixation and CSRF protection.
It's based on Java 8, Spring Web MVC 4 and on the **[pac4j security engine](https://github.com/pac4j/pac4j) v2.0**. It's available under the Apache 2 license.

[**Main concepts and components:**](http://www.pac4j.org/docs/main-concepts-and-components.html)

1) A [**client**](http://www.pac4j.org/docs/clients.html) represents an authentication mechanism. It performs the login process and returns a user profile. An indirect client is for UI authentication while a direct client is for web services authentication:

&#9656; OAuth - SAML - CAS - OpenID Connect - HTTP - OpenID - Google App Engine - LDAP - SQL - JWT - MongoDB - Stormpath - IP address

2) An [**authorizer**](http://www.pac4j.org/docs/authorizers.html) is meant to check authorizations on the authenticated user profile(s) or on the current web context:

&#9656; Roles / permissions - Anonymous / remember-me / (fully) authenticated - Profile type, attribute -  CORS - CSRF - Security headers - IP address, HTTP method

3) The `SecurityInterceptor` protects an url by checking that the user is authenticated and that the authorizations are valid, according to the clients and authorizers configuration. If the user is not authenticated, it performs authentication for direct clients or starts the login process for indirect clients

4) The `CallbackController` finishes the login process for an indirect client

5) The `ApplicationLogoutController` logs out the user from the application.

==

Just follow these easy steps to secure your Spring web application:

### 1) Add the required dependencies (`spring-webmvc-pac4j` + `pac4j-*` libraries)

You need to add a dependency on:
 
- the `spring-webmvc-pac4j` library (<em>groupId</em>: **org.pac4j**, *version*: **2.0.0-SNAPSHOT**)
- the appropriate `pac4j` [submodules](http://www.pac4j.org/docs/clients.html) (<em>groupId</em>: **org.pac4j**, *version*: **2.0.0-SNAPSHOT**): `pac4j-oauth` for OAuth support (Facebook, Twitter...), `pac4j-cas` for CAS support, `pac4j-ldap` for LDAP authentication, etc.

All released artifacts are available in the [Maven central repository](http://search.maven.org/#search%7Cga%7C1%7Cpac4j).

---

### 2) Define the configuration (`Config` + `Client` + `Authorizer`)

The configuration (`org.pac4j.core.config.Config`) contains all the clients and authorizers required by the application to handle security.

It can be built via a Spring context file or a Spring configuration class:

#### Spring context file:

```xml
    <bean id="oidClient" class="org.pac4j.oidc.client.GoogleOidcClient">
        <property name="clientID" value="googleId" />
        <property name="secret" value="googleSecret" />
    </bean>
    <bean id="samlConfig" class="org.pac4j.saml.client.SAML2ClientConfiguration">
        <constructor-arg name="keystorePath" value="resource:samlKeystore.jks" />
        <constructor-arg name="keystorePassword" value="pac4j-demo-passwd" />
        <constructor-arg name="privateKeyPassword" value="pac4j-demo-passwd" />
        <constructor-arg name="identityProviderMetadataPath" value="resource:metadata-okta.xml" />
        <property name="maximumAuthenticationLifetime" value="3600" />
        <property name="serviceProviderEntityId" value="http://localhost:8080/callback?client_name=SAML2Client" />
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
        <constructor-arg name="loginUrl" value="http://localhost:8080/loginForm" />
        <constructor-arg name="usernamePasswordAuthenticator" ref="testAuthenticator" />
    </bean>
    <bean id="casClient" class="org.pac4j.cas.client.CasClient">
        <property name="casLoginUrl" value="https://casserverpac4j.herokuapp.com/login" />
    </bean>
    <bean id="parameterClient" class="org.pac4j.http.client.direct.ParameterClient">
        <constructor-arg name="parameterName" value="token" />
        <constructor-arg name="tokenAuthenticator">
            <bean class="org.pac4j.jwt.credentials.authenticator.JwtAuthenticator">
                <constructor-arg name="signingSecret" value="12345678901234567890123456789012" />
                <constructor-arg name="encryptionSecret" value="12345678901234567890123456789012" />
            </bean>
        </constructor-arg>
    </bean>
    <bean id="directBasicAuthClient" class="org.pac4j.http.client.direct.DirectBasicAuthClient">
        <constructor-arg name="usernamePasswordAuthenticator" ref="testAuthenticator" />
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
                <ref bean="casClient" />
                <ref bean="parameterClient" />
                <ref bean="directBasicAuthClient" />
            </list>
        </constructor-arg>
    </bean>
    <bean id="adminRoleAuthorizer" class="org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer">
        <constructor-arg name="roles" value="ROLE_ADMIN" />
    </bean>
    <bean id="customAuthorizer" class="org.pac4j.demo.spring.CustomAuthorizer">
    </bean>
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

    @Value("${salt}")
    private String salt;

    @Bean
    public Config config() {
        final GoogleOidcClient oidcClient = new GoogleOidcClient();
        oidcClient.setClientID("googldId");
        oidcClient.setSecret("googleSecret");

        final SAML2ClientConfiguration cfg = new SAML2ClientConfiguration("resource:samlKeystore.jks", "pac4j-demo-passwd", "pac4j-demo-passwd", "resource:metadata-okta.xml");
        cfg.setMaximumAuthenticationLifetime(3600);
        cfg.setServiceProviderEntityId("http://localhost:8080/callback?client_name=SAML2Client");
        cfg.setServiceProviderMetadataPath("sp-metadata.xml");
        final SAML2Client saml2Client = new SAML2Client(cfg);

        final FacebookClient facebookClient = new FacebookClient("fbId", "fbSecret");
        final TwitterClient twitterClient = new TwitterClient("twId", "twSecret");

        final FormClient formClient = new FormClient("http://localhost:8080/loginForm.jsp", new SimpleTestUsernamePasswordAuthenticator());

        final CasClient casClient = new CasClient("https://casserverpac4j.herokuapp.com/login");

        ParameterClient parameterClient = new ParameterClient("token", new JwtAuthenticator(salt));

        final DirectBasicAuthClient directBasicAuthClient = new DirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator());

        final Clients clients = new Clients("http://localhost:8080/callback", oidcClient, saml2Client, facebookClient,
                twitterClient, formClient, casClient, parameterClient, directBasicAuthClient);

        final Config config = new Config(clients);
        config.addAuthorizer("admin", new RequireAnyRoleAuthorizer("ROLE_ADMIN"));
        config.addAuthorizer("custom", new CustomAuthorizer());
        return config;
    }
}
```

`http://localhost:8080/callback` is the url of the callback endpoint, which is only necessary for indirect clients.

Notice that you can define specific [matchers](http://www.pac4j.org/docs/matchers.html) via the `addMatcher(name, Matcher)` method.

---

### 3) Protect urls (`SecurityInterceptor`)

You can protect (authentication + authorizations) the urls of your Spring application by using the `SecurityInterceptor` and defining the appropriate mapping. It has the following behaviour:

1) If the HTTP request matches the `matchers` configuration (or no `matchers` are defined), the security is applied. Otherwise, the user is automatically granted access.

2) First, if the user is not authenticated (no profile) and if some clients have been defined in the `clients` parameter, a login is tried for the direct clients.

3) Then, if the user has a profile, authorizations are checked according to the `authorizers` configuration. If the authorizations are valid, the user is granted access. Otherwise, a 403 error page is displayed.

4) Finally, if the user is still not authenticated (no profile), he is redirected to the appropriate identity provider if the first defined client is an indirect one in the `clients` configuration. Otherwise, a 401 error page is displayed.


The following parameters are available:

1) `clients` (optional): the list of client names (separated by commas) used for authentication:
- in all cases, this filter requires the user to be authenticated. Thus, if the `clients` is blank or not defined, the user must have been previously authenticated
- if the `client_name` request parameter is provided, only this client (if it exists in the `clients`) is selected.

2) `authorizers` (optional): the list of authorizer names (separated by commas) used to check authorizations:
- if the `authorizers` is blank or not defined, no authorization is checked
- the following authorizers are available by default (without defining them in the configuration):
  * `isFullyAuthenticated` to check if the user is authenticated but not remembered, `isRemembered` for a remembered user, `isAnonymous` to ensure the user is not authenticated, `isAuthenticated` to ensure the user is authenticated (not necessary by default unless you use the `AnonymousClient`)
  * `hsts` to use the `StrictTransportSecurityHeader` authorizer, `nosniff` for `XContentTypeOptionsHeader`, `noframe` for `XFrameOptionsHeader `, `xssprotection` for `XSSProtectionHeader `, `nocache` for `CacheControlHeader ` or `securityHeaders` for the five previous authorizers
  * `csrfToken` to use the `CsrfTokenGeneratorAuthorizer` with the `DefaultCsrfTokenGenerator` (it generates a CSRF token and saves it as the `pac4jCsrfToken` request attribute and in the `pac4jCsrfToken` cookie), `csrfCheck` to check that this previous token has been sent as the `pac4jCsrfToken` header or parameter in a POST request and `csrf` to use both previous authorizers.

3) `matchers` (optional): the list of matcher names (separated by commas) that the request must satisfy to check authentication / authorizations

4) `multiProfile` (optional): it indicates whether multiple authentications (and thus multiple profiles) must be kept at the same time (`false` by default).


#### Spring context file:

``` xml
<mvc:interceptors>
    <mvc:interceptor>
        <mvc:mapping path="/facebookadmin/*" />
        <bean class="org.pac4j.springframework.web.SecurityInterceptor">
            <constructor-arg name="config" ref="config" />
            <constructor-arg name="clients" value="FacebookClient" />
            <constructor-arg name="authorizers" value="admin" />
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
        registry.addInterceptor(new SecurityInterceptor(config, "FacebookClient", "admin")).addPathPatterns("/facebookadmin/*");
        ...
    }
}

```

---

### 4) Define the callback endpoint only for indirect clients (`CallbackController`)

For indirect clients (like Facebook), the user is redirected to an external identity provider for login and then back to the application.
Thus, a callback endpoint is required in the application. It is managed by the `CallbackController` which has the following behaviour:

1) the credentials are extracted from the current request to fetch the user profile (from the identity provider) which is then saved in the web session

2) finally, the user is redirected back to the originally requested url (or to the `defaultUrl`).


The following properties are available:

1) `pac4j.callback.defaultUrl` (optional): it's the default url after login if no url was originally requested (`/` by default)

2) `pac4j.callback.multiProfile` (optional): it indicates whether multiple authentications (and thus multiple profiles) must be kept at the same time (`false` by default)

3) `pac4j.callback.renewSession` (optional): it indicates whether the web session must be renewed after login, to avoid session hijacking (`true` by default).


The `CallbackController` must be defined by class scanning to be available on the `/callback` url:

#### Spring context file:

```xml
<context:component-scan base-package="org.pac4j.springframework.web" />
```

#### Spring configuration class:

```java
@ComponentScan(basePackages = "org.pac4j.springframework.web")
```

---

### 5) Get the user profile (`ProfileManager`)

You can get the profile of the authenticated user using `profileManager.get(true)` (`false` not to use the session, but only the current HTTP request).
You can test if the user is authenticated using `profileManager.isAuthenticated()`.
You can get all the profiles of the authenticated user (if ever multiple ones are kept) using `profileManager.getAll(true)`.

Example:

```java
WebContext context = new J2EContext(request, response);
ProfileManager manager = new ProfileManager(context);
Optional<CommonProfile> profile = manager.get(true);
```

The retrieved profile is at least a `CommonProfile`, from which you can retrieve the most common attributes that all profiles share. But you can also cast the user profile to the appropriate profile according to the provider used for authentication. For example, after a Facebook authentication:

```java
FacebookProfile facebookProfile = (FacebookProfile) commonProfile;
```

---

### 6) Logout (`LogoutController`)

The `LogoutController` can handle:
 
- the local logout by removing the pac4j profiles from the session (it can be used for the front-channel logout from the identity provider in case of a central logout)
- the central logout by calling the identity provider logout endpoint.


It has the following behaviour:

1) If the `localLogout` property is `true`, the pac4j profiles are removed from the web session (and the web session is destroyed if the `destroySession` property is `true`)

2) A post logout action is computed as the redirection to the `url` request parameter if it matches the `logoutUrlPattern` or to the `defaultUrl` if it is defined or as a blank page otherwise

3) If the `centralLogout` property is `true`, the user is redirected to the identity provider for a central logout and
then optionally to the post logout redirection URL (if it's supported by the identity provider and if it's an absolute URL).
If no central logout is defined, the post logout action is performed directly.


The following properties are available:

1) `pac4j.logout.defaultUrl` (optional): the default logout url if no `url` request parameter is provided or if the `url` does not match the `logoutUrlPattern` (not defined by default)

2) `pac4j.logout.logoutUrlPattern` (optional): the logout url pattern that the `url` parameter must match (only relative urls are allowed by default)

3) `pac4j.logout.localLogout` (optional): whether a local logout must be performed (`true` by default)

4) `pac4j.logout.destroySession` (optional):  whether we must destroy the web session during the local logout (`false` by default) (`false` by default) (`false` by default)

5) `pac4j.logout.centralLogout` (optional): whether a central logout must be performed (`false` by default).

The `LogoutController` must be defined by classpath scanning to be available on the `/logout` url:

#### Spring context file:

```xml
<context:component-scan base-package="org.pac4j.springframework.web" />
```

#### Spring configuration class:

```java
@ComponentScan(basePackages = "org.pac4j.springframework.web")
```

---

## Migration guide

### 1.1 -> 2.0

The `ApplicationLogoutController` has been renamed as `LogoutController` and now handles both the application and identity provider logouts.

### 1.0 -> 1.1

The `RequiresAuthenticationInterceptor` is now named `SecurityInterceptor`.

The `ApplicationLogoutController` behaviour has slightly changed: even without any `url` request parameter, the user will be redirected to the `defaultUrl` if it has been defined.


## Demos

The demo webapps for Spring Web MVC without Spring Boot: [spring-webmvc-pac4j-demo](https://github.com/pac4j/spring-webmvc-pac4j-demo) or with Spring Boot: [spring-webmvc-pac4j-boot-demo](https://github.com/pac4j/spring-webmvc-pac4j-boot-demo) are available for tests and implement many authentication mechanisms: Facebook, Twitter, form, basic auth, CAS, SAML, OpenID Connect, JWT...


## Release notes

See the [release notes](https://github.com/pac4j/spring-webmvc-pac4j/wiki/Release-Notes). Learn more by browsing the [spring-webmvc-pac4j Javadoc](http://www.javadoc.io/doc/org.pac4j/spring-webmvc-pac4j/2.0.0) and the [pac4j Javadoc](http://www.pac4j.org/apidocs/pac4j/2.0.0/index.html).


## Need help?

If you have any question, please use the following mailing lists:

- [pac4j users](https://groups.google.com/forum/?hl=en#!forum/pac4j-users)
- [pac4j developers](https://groups.google.com/forum/?hl=en#!forum/pac4j-dev)


## Development

The version 2.0.0-SNAPSHOT is under development.

Maven artifacts are built via Travis: [![Build Status](https://travis-ci.org/pac4j/spring-webmvc-pac4j.png?branch=master)](https://travis-ci.org/pac4j/spring-webmvc-pac4j) and available in the [Sonatype snapshots repository](https://oss.sonatype.org/content/repositories/snapshots/org/pac4j). This repository must be added in the Maven `pom.xml` file for example:

```xml
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
```
