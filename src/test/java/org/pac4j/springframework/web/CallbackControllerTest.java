package org.pac4j.springframework.web;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.FrameworkParameters;
import org.pac4j.core.engine.CallbackLogic;
import org.pac4j.jee.context.JEEFrameworkParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests the {@link CallbackController} mappings with a custom callback path.
 *
 * @author Jerome LELEU
 * @since 8.0.4
 */
@SpringJUnitWebConfig(CallbackControllerTest.TestConfig.class)
@TestPropertySource(properties = {"pac4j.callback.path=/cb", "pac4j.callback.defaultClient=TestClient"})
class CallbackControllerTest {

    /**
     * Records the callback logic invocations instead of performing a real login.
     */
    static class RecordingCallbackLogic implements CallbackLogic {

        final List<String> requestUris = new ArrayList<>();
        final List<String> defaultClients = new ArrayList<>();

        @Override
        public Object perform(final Config config, final String defaultUrl, final Boolean renewSession,
                              final String defaultClient, final FrameworkParameters parameters) {
            final HttpServletRequest request = ((JEEFrameworkParameters) parameters).getRequest();
            requestUris.add(request.getRequestURI());
            defaultClients.add(defaultClient);
            return null;
        }
    }

    /**
     * Not annotated with <code>@Configuration</code> on purpose: this package is component-scanned
     * by <code>Pac4jSecurityConfig</code> and this configuration must not leak into the other tests.
     */
    @EnableWebMvc
    static class TestConfig {

        @Bean
        static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }

        @Bean
        RecordingCallbackLogic callbackLogic() {
            return new RecordingCallbackLogic();
        }

        @Bean
        Config config(final RecordingCallbackLogic callbackLogic) {
            final Config config = new Config();
            config.setCallbackLogic(callbackLogic);
            return config;
        }

        @Bean
        CallbackController callbackController() {
            return new CallbackController();
        }
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RecordingCallbackLogic callbackLogic;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        callbackLogic.requestUris.clear();
        callbackLogic.defaultClients.clear();
    }

    @Test
    void customCallbackPathIsMapped() throws Exception {
        mockMvc.perform(get("/cb")).andExpect(status().isOk());

        assertEquals(List.of("/cb"), callbackLogic.requestUris);
        assertEquals(List.of("TestClient"), callbackLogic.defaultClients);
    }

    @Test
    void customCallbackPathWithClientNameIsMapped() throws Exception {
        mockMvc.perform(get("/cb/MyClient")).andExpect(status().isOk());

        assertEquals(List.of("/cb/MyClient"), callbackLogic.requestUris);
    }

    @Test
    void defaultCallbackPathsAreNoLongerMappedWhenCustomized() throws Exception {
        mockMvc.perform(get("/callback")).andExpect(status().isNotFound());
        mockMvc.perform(get("/callback/MyClient")).andExpect(status().isNotFound());

        assertEquals(List.of(), callbackLogic.requestUris);
    }
}
