package backend.dto;

import java.util.ArrayList;
import java.util.List;

public class FormDTO {
    private final int formId;
    private final String formName;
    private final String layoutType;
    private final List<FieldDTO> fields;

    public FormDTO(int formId, String formName, String layoutType) {
        this.formId = formId;
        this.formName = formName;
        this.layoutType = layoutType;
        this.fields = new ArrayList<>();
    }

    public int getFormId() {
        return formId;
    }

    public String getFormName() {
        return formName;
    }

    public String getLayoutType() {
        return layoutType;
    }

    public List<FieldDTO> getFields() {
        return fields;
    }

    public void addField(FieldDTO field) {
        fields.add(field);
    }

    public void removeField(int fieldId) {
        fields.removeIf(field -> field.getFieldId() == fieldId);
    }

    @Override
    public String toString() {
        return "Form ID: " + formId +
               ", Name: " + formName +
               ", Layout: " + layoutType;
    }
}