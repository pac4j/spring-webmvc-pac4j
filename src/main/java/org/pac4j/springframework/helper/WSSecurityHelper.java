package org.pac4j.springframework.helper;

/**
 * The helper to use for web services.
 *
 * @author Jerome Leleu
 * @since 3.2.0
 */
public class WSSecurityHelper extends CommonSecurityHelper {

    @Override
    protected boolean getDefaultReadFromSession() {
        return false;
    }
}
