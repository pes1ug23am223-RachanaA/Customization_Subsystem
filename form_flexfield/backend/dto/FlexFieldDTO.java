package backend.dto;

public class FlexFieldDTO {
    private final int fieldId;
    private final String fieldName;
    private final String fieldType;
    private final String segments;

    public FlexFieldDTO(int fieldId, String fieldName, String fieldType, String segments) {
        this.fieldId = fieldId;
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.segments = segments;
    }

    public int getFieldId() {
        return fieldId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public String getSegments() {
        return segments;
    }

    @Override
    public String toString() {
        return fieldName + " [" + fieldType + "]";
    }
}