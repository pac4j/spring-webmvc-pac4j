package org.pac4j.springframework.controller;

/**
 * The controller to inherit from for REST controllers.
 *
 * @author Jerome Leleu
 * @since 3.2.0
 */
public class AbstractRestController extends AbstractParentController {

    @Override
    protected boolean getDefaultReadFromSession() {
        return false;
    }
}
