package org.pac4j.springframework.helper;

/**
 * The helper to use for web applications.
 *
 * @author Jerome Leleu
 * @since 3.2.0
 */
public class UISecurityHelper extends CommonSecurityHelper {

    @Override
    protected boolean getDefaultReadFromSession() {
        return true;
    }
}
