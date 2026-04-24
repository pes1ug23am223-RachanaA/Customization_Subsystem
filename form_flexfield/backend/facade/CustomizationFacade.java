package backend.facade;

import backend.controller.CustomizationController;
import backend.dto.FieldDTO;

import java.util.List;

/**
 * FACADE: CustomizationFacade
 * Subsystem: Customization — Form Designer & Flexfield Manager
 *
 * Provides a simplified entry point to the Form and Flexfield services.
 * Also exposes getValues() for Onboarding integration:
 *   IEmployeeIntegration.assignRole() validates roles against
 *   lookup.getValues("DEPARTMENT") — routed through this facade.
 */
public class CustomizationFacade {

    private final CustomizationController controller;

    public CustomizationFacade() {
        controller = CustomizationController.getInstance();
    }

    public int createForm(String name, String layout) {
        return controller.getFormService()
                .createForm(name, layout);
    }

    public void addFieldToForm(int formId, FieldDTO field) {
        controller.getFormService()
                .addField(formId, field);
    }

    public void createFlexfield(String name, String type, String segments) {
        controller.getFlexfieldService()
                .addField(name, type, segments);
    }

    /**
     * Returns valid values for a flexfield by ID.
     * Used by Onboarding & Offboarding subsystem to validate
     * department/role strings before calling assignRole().
     *
     * @param fieldId  ID of the flexfield (e.g. DEPARTMENT flexfield ID)
     * @return list of valid string values
     */
    public List<String> getValues(int fieldId) {
        return controller.getFlexfieldService()
                .getValues(fieldId);
    }
}
