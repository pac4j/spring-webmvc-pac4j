package org.pac4j.springframework.helper;

/**
 * The helper to use in a REST API.
 *
 * @author Jerome Leleu
 * @since 3.2.0
 */
public class RestSecurityHelper extends CommonSecurityHelper {

    @Override
    protected boolean getDefaultReadFromSession() {
        return false;
    }
}
