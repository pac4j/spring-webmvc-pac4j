package org.pac4j.springframework.component;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the {@link ComponentConfig} wiring.
 *
 * @author Jerome LELEU
 * @since 8.0.4
 */
class ComponentConfigTest {

    @Configuration
    static class WithConfig {

        @Bean
        Config config() {
            return new Config();
        }
    }

    private AnnotationConfigWebApplicationContext context;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        context.close();
    }

    @Test
    void startupFailsWithoutConfig() {
        context.register(ComponentConfig.class);

        assertThrows(UnsatisfiedDependencyException.class, context::refresh);
    }

    @Test
    void componentsAreBuiltFromTheCurrentRequest() {
        context.register(WithConfig.class, ComponentConfig.class);
        context.refresh();

        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/index.html");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));

        final JEEContext webContext = context.getBean(JEEContext.class);
        assertEquals("/index.html", webContext.getPath());

        assertNotNull(context.getBean(SessionStore.class));

        final ProfileManager profileManager = context.getBean(ProfileManager.class);
        assertEquals(0, profileManager.getProfiles().size());

        // the profile manager reads the profiles of the current request
        final CommonProfile profile = new CommonProfile();
        profile.setId("jerome");
        final LinkedHashMap<String, UserProfile> profiles = new LinkedHashMap<>();
        profiles.put("TestClient", profile);
        request.setAttribute(Pac4jConstants.USER_PROFILES, profiles);
        assertEquals(1, profileManager.getProfiles().size());
        assertEquals("jerome", profileManager.getProfile().orElseThrow().getId());
    }
}
