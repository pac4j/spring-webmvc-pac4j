package org.pac4j.springframework.controller;

/**
 * The controller to inherit from for web controllers.
 *
 * @author Jerome Leleu
 * @since 3.2.0
 */
public class AbstractController extends AbstractParentController {

    @Override
    protected boolean getDefaultReadFromSession() {
        return true;
    }
}
