package org.pac4j.springframework.helper;

/**
 * The helper to use in a web application.
 *
 * @author Jerome Leleu
 * @since 3.2.0
 */
public class WebSecurityHelper extends CommonSecurityHelper {

    @Override
    protected boolean getDefaultReadFromSession() {
        return true;
    }
}
