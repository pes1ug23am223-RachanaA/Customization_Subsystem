package backend.controller;

import backend.factory.RepositoryFactory;
import backend.service.*;

public class CustomizationController {

    private static CustomizationController instance;

    private final FormService formService;
    private final FlexfieldService flexfieldService;

    private CustomizationController() {
        formService = new FormServiceImpl(
                RepositoryFactory.createFormRepository());

        flexfieldService = new FlexfieldServiceImpl(
                RepositoryFactory.createFlexfieldRepository());
    }

    public static synchronized CustomizationController getInstance() {
        if (instance == null) {
            instance = new CustomizationController();
        }
        return instance;
    }

    public FormService getFormService() {
        return formService;
    }

    public FlexfieldService getFlexfieldService() {
        return flexfieldService;
    }
}