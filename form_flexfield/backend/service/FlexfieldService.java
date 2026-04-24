package backend.service;

import backend.dto.FlexFieldDTO;

import java.util.List;

public interface FlexfieldService {

    void addField(String name, String type, String segments);

    List<FlexFieldDTO> getAllFields();

    void deleteField(int fieldId);

    boolean validateField(int fieldId);

    /**
     * Returns all valid values for a given flexfield (e.g. DEPARTMENT values).
     * Used by the Onboarding integration to validate role assignments.
     */
    List<String> getValues(int fieldId);
}
