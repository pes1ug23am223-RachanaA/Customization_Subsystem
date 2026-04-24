package backend.repository;

import backend.dto.FieldDTO;
import backend.dto.FormDTO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CLASS: MockFormRepository
 * Subsystem: Customization — Form Designer
 *
 * PURPOSE:
 *   In-memory mock implementation of IFormRepository.
 *   Used for testing when DB_INTEGRATION = false.
 *   Stores all forms in memory; data is lost on restart.
 */
public class MockFormRepository implements IFormRepository {

    private final Map<Integer, FormDTO> forms = new HashMap<>();
    private int formIdCounter = 1;

    @Override
    public int createForm(String name, String layoutType) {
        int formId = formIdCounter++;
        FormDTO form = new FormDTO(formId, name, layoutType);
        forms.put(formId, form);
        System.out.println("[MockFormRepository] Created form: " + name + " (id=" + formId + ")");
        return formId;
    }

    @Override
    public FormDTO getFormById(int formId) {
        FormDTO form = forms.get(formId);
        if (form == null) {
            System.out.println("[MockFormRepository] Form " + formId + " not found");
        }
        return form;
    }

    @Override
    public List<FormDTO> getAllForms() {
        return new ArrayList<>(forms.values());
    }

    @Override
    public void updateForm(int formId, String newName) {
        FormDTO form = forms.get(formId);
        if (form != null) {
            forms.put(formId, new FormDTO(formId, newName, form.getLayoutType()));
            System.out.println("[MockFormRepository] Updated form " + formId + " to: " + newName);
        }
    }

    @Override
    public void deleteForm(int formId) {
        forms.remove(formId);
        System.out.println("[MockFormRepository] Deleted form: " + formId);
    }

    @Override
    public void addFieldToForm(int formId, FieldDTO field) {
        FormDTO form = forms.get(formId);
        if (form != null) {
            form.addField(field);
            System.out.println("[MockFormRepository] Added field to form " + formId + ": " + field.getFieldName());
        }
    }

    @Override
    public void removeFieldFromForm(int formId, int fieldId) {
        FormDTO form = forms.get(formId);
        if (form != null) {
            form.removeField(fieldId);
            System.out.println("[MockFormRepository] Removed field " + fieldId + " from form " + formId);
        }
    }

    @Override
    public List<FieldDTO> getFieldsByForm(int formId) {
        FormDTO form = forms.get(formId);
        if (form != null) {
            return new ArrayList<>(form.getFields());
        }
        return new ArrayList<>();
    }
}
