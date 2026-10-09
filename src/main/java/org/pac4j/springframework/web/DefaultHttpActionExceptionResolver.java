package org.pac4j.springframework.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pac4j.core.adapter.FrameworkAdapter;
import org.pac4j.core.config.Config;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.jee.context.JEEContext;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

/**
 * <p>This exception resolver performs the pac4j {@link HttpAction}s thrown by the handlers
 * (for example by the {@code @RequireAnyRole} and {@code @RequireAllRoles} annotations) on the HTTP response,
 * using the {@code HttpActionAdapter} of the configuration.</p>
 *
 * <p>It runs last so that the {@code @ExceptionHandler} methods of the application keep precedence.</p>
 *
 * @author Jerome LELEU
 * @since 8.0.4
 */
public class DefaultHttpActionExceptionResolver implements HandlerExceptionResolver, Ordered {

    private final Config config;

    /**
     * Constructor.
     *
     * @param config the config
     */
    public DefaultHttpActionExceptionResolver(final Config config) {
        this.config = config;
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    @Override
    public ModelAndView resolveException(final HttpServletRequest request, final HttpServletResponse response,
                                         final Object handler, final Exception ex) {
        if (ex instanceof HttpAction action) {
            FrameworkAdapter.INSTANCE.applyDefaultSettingsIfUndefined(config);

            config.getHttpActionAdapter().adapt(action, new JEEContext(request, response));
            // an empty ModelAndView means that the response has been handled
            return new ModelAndView();
        }
        return null;
    }
}
