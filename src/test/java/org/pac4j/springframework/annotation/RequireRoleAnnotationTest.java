package org.pac4j.springframework.annotation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pac4j.core.config.Config;
import org.pac4j.core.exception.http.ForbiddenAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.springframework.config.Pac4jSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests the {@link RequireAnyRole} and {@link RequireAllRoles} annotations and the HTTP status they produce.
 *
 * @author Jerome LELEU
 * @since 8.0.4
 */
@SpringJUnitWebConfig(RequireRoleAnnotationTest.SecurityConfig.class)
class RequireRoleAnnotationTest {

    @RestController
    static class AdminController {

        @GetMapping("/admin/any")
        @RequireAnyRole({"ROLE_ADMIN", "ROLE_SUPERVISOR"})
        String any() {
            return "any";
        }

        @GetMapping("/admin/all")
        @RequireAllRoles({"ROLE_ADMIN", "ROLE_SUPERVISOR"})
        String all() {
            return "all";
        }
    }

    /**
     * A controller with its own handling of the forbidden action, which must keep precedence.
     */
    @RestController
    static class CustomController {

        @GetMapping("/custom/any")
        @RequireAnyRole("ROLE_ADMIN")
        String any() {
            return "any";
        }

        @ExceptionHandler(ForbiddenAction.class)
        @ResponseStatus(HttpStatus.CONFLICT)
        String forbidden() {
            return "custom";
        }
    }

    @Configuration
    @EnableWebMvc
    static class SecurityConfig extends Pac4jSecurityConfig {

        @Bean
        static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }

        @Bean
        AdminController adminController() {
            return new AdminController();
        }

        @Bean
        CustomController customController() {
            return new CustomController();
        }

        @Override
        public Config config() {
            return new Config();
        }
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private static MockHttpSession sessionWithRoles(final String... roles) {
        final CommonProfile profile = new CommonProfile();
        profile.setId("jerome");
        for (final String role : roles) {
            profile.addRole(role);
        }
        final LinkedHashMap<String, UserProfile> profiles = new LinkedHashMap<>();
        profiles.put("TestClient", profile);
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute(Pac4jConstants.USER_PROFILES, profiles);
        return session;
    }

    @Test
    void anonymousUserGets401() throws Exception {
        mockMvc.perform(get("/admin/any")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/admin/all")).andExpect(status().isUnauthorized());
    }

    @Test
    void userWithoutTheRolesGets403() throws Exception {
        final MockHttpSession session = sessionWithRoles("ROLE_USER");

        mockMvc.perform(get("/admin/any").session(session)).andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/all").session(session)).andExpect(status().isForbidden());
    }

    @Test
    void userWithOneRoleOnlyPassesRequireAnyRole() throws Exception {
        final MockHttpSession session = sessionWithRoles("ROLE_ADMIN");

        final String body = mockMvc.perform(get("/admin/any").session(session)).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        assertEquals("any", body);
        mockMvc.perform(get("/admin/all").session(session)).andExpect(status().isForbidden());
    }

    @Test
    void userWithAllRolesPassesBothAnnotations() throws Exception {
        final MockHttpSession session = sessionWithRoles("ROLE_ADMIN", "ROLE_SUPERVISOR");

        mockMvc.perform(get("/admin/any").session(session)).andExpect(status().isOk());
        final String body = mockMvc.perform(get("/admin/all").session(session)).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        assertEquals("all", body);
    }

    @Test
    void applicationExceptionHandlerKeepsPrecedence() throws Exception {
        final String body = mockMvc.perform(get("/custom/any").session(sessionWithRoles("ROLE_USER")))
            .andExpect(status().isConflict()).andReturn().getResponse().getContentAsString();
        assertEquals("custom", body);
    }
}
