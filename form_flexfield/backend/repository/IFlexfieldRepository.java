package backend.repository;

import backend.dto.FlexFieldDTO;

import java.util.List;

/**
 * INTERFACE: IFlexfieldRepository
 *
 * Aligned with DB team's CustomizationRepositoryImpl signature.
 * Key changes from original:
 *   - updateFieldSegment now matches DB: (int fieldId, int segmentIndex, String value)
 *   - getValues(int fieldId) added — used by IEmployeeIntegration.assignRole()
 *     to validate role strings against DEPARTMENT lookup
 */
public interface IFlexfieldRepository {

    void addField(String name, String type, String segments);

    FlexFieldDTO getFieldById(int fieldId);

    List<FlexFieldDTO> getAllFlexfields();

    void removeField(int fieldId);

    /**
     * Update a specific segment value within a flexfield.
     * @param fieldId       The flexfield to update
     * @param segmentIndex  Index of the segment to change (0-based)
     * @param value         New segment value
     */
    void updateFieldSegment(int fieldId, int segmentIndex, String value);

    boolean validateField(int fieldId);

    /**
     * Return all valid values for a flexfield by name (e.g. "DEPARTMENT").
     * Used by Onboarding integration to validate role/department assignments.
     * @param fieldId  ID of the lookup-type flexfield
     * @return list of valid string values
     */
    List<String> getValues(int fieldId);
}
