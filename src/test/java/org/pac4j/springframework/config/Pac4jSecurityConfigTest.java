package org.pac4j.springframework.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.FrameworkParameters;
import org.pac4j.core.engine.SecurityGrantedAccessAdapter;
import org.pac4j.core.engine.SecurityLogic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests that the <code>Config</code> built by {@link Pac4jSecurityConfig#config()} is a singleton bean
 * shared with the security interceptors.
 *
 * @author Jerome LELEU
 * @since 8.0.4
 */
class Pac4jSecurityConfigTest {

    /**
     * Records the configurations it is called with and always grants access.
     */
    static class RecordingSecurityLogic implements SecurityLogic {

        final List<Config> configs = new ArrayList<>();
        final List<String> clients = new ArrayList<>();

        @Override
        public Object perform(final Config config, final SecurityGrantedAccessAdapter securityGrantedAccessAdapter,
                              final String clients, final String authorizers, final String matchers,
                              final FrameworkParameters parameters) {
            this.configs.add(config);
            this.clients.add(clients);
            return true;
        }
    }

    @RestController
    static class ProtectedController {

        @GetMapping("/protected/data")
        String data() {
            return "data";
        }
    }

    /**
     * The security configuration: <code>config()</code> is implemented without any annotation.
     */
    @Configuration
    @EnableWebMvc
    static class SecurityConfig extends Pac4jSecurityConfig {

        static final RecordingSecurityLogic SECURITY_LOGIC = new RecordingSecurityLogic();

        static final AtomicInteger CONFIG_CALLS = new AtomicInteger();

        @Bean
        static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }

        @Bean
        ProtectedController protectedController() {
            return new ProtectedController();
        }

        @Override
        public Config config() {
            CONFIG_CALLS.incrementAndGet();
            final Config config = new Config();
            config.setSecurityLogic(SECURITY_LOGIC);
            return config;
        }

        @Override
        public void addInterceptors(final InterceptorRegistry registry) {
            addSecurity(registry, "TestClient").addPathPatterns("/protected/**");
        }
    }

    /**
     * The security configuration as documented so far: <code>config()</code> is annotated with <code>@Bean</code> again.
     */
    @Configuration
    static class LegacySecurityConfig extends SecurityConfig {

        @Override
        @Bean
        public Config config() {
            return super.config();
        }
    }

    private AnnotationConfigWebApplicationContext context;

    @AfterEach
    void tearDown() {
        context.close();
    }

    private void checkSingleConfig(final Class<?> configClass) throws Exception {
        SecurityConfig.CONFIG_CALLS.set(0);
        SecurityConfig.SECURITY_LOGIC.configs.clear();
        SecurityConfig.SECURITY_LOGIC.clients.clear();

        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(configClass);
        context.refresh();

        final MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        final String body = mockMvc.perform(get("/protected/data")).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        assertEquals("data", body);

        assertEquals(1, SecurityConfig.CONFIG_CALLS.get());
        assertEquals(1, SecurityConfig.SECURITY_LOGIC.configs.size());
        assertSame(context.getBean(Config.class), SecurityConfig.SECURITY_LOGIC.configs.get(0));
        assertEquals(List.of("TestClient"), SecurityConfig.SECURITY_LOGIC.clients);
    }

    @Test
    void configIsASingletonBeanWithoutAnnotationInTheSubclass() throws Exception {
        checkSingleConfig(SecurityConfig.class);
    }

    @Test
    void configIsASingletonBeanWithTheAnnotationInTheSubclass() throws Exception {
        checkSingleConfig(LegacySecurityConfig.class);
    }
}
