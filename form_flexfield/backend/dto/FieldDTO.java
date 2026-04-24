package backend.dto;

public class FieldDTO {
    private int fieldId;
    private String fieldName;
    private String fieldType;
    private boolean mandatory;
    private String defaultValue;

    // Original 4-param constructor (backward compatible)
    public FieldDTO(int fieldId, String fieldName, String fieldType, boolean mandatory) {
        this.fieldId = fieldId;
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.mandatory = mandatory;
        this.defaultValue = null;
    }

    // Extended 5-param constructor (used by DbFormRepository)
    public FieldDTO(int fieldId, String fieldName, String fieldType, boolean mandatory, String defaultValue) {
        this.fieldId = fieldId;
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.mandatory = mandatory;
        this.defaultValue = defaultValue;
    }

    public int getFieldId()      { return fieldId; }
    public String getFieldName() { return fieldName; }
    public String getFieldType() { return fieldType; }
    public boolean isMandatory() { return mandatory; }
    public String getDefaultValue() { return defaultValue; }

    @Override
    public String toString() {
        return fieldName + " (" + fieldType + ")";
    }
}
