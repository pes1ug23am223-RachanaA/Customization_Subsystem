package backend.service;

import backend.dto.FlexFieldDTO;
import backend.repository.IFlexfieldRepository;

import java.util.List;

public class FlexfieldServiceImpl implements FlexfieldService {

    private final IFlexfieldRepository repository;

    public FlexfieldServiceImpl(IFlexfieldRepository repository) {
        this.repository = repository;
    }

    @Override
    public void addField(String name, String type, String segments) {
        repository.addField(name, type, segments);
    }

    @Override
    public List<FlexFieldDTO> getAllFields() {
        return repository.getAllFlexfields();
    }

    @Override
    public void deleteField(int fieldId) {
        repository.removeField(fieldId);
    }

    @Override
    public boolean validateField(int fieldId) {
        return repository.validateField(fieldId);
    }

    /**
     * Returns valid values for a given flexfield.
     * Used by the Onboarding integration layer to validate
     * role/department assignments via IEmployeeIntegration.assignRole().
     *
     * Example: getValues(3) returns ["HR","Finance","Engineering","Sales"]
     *          for the DEPARTMENT flexfield.
     */
    public List<String> getValues(int fieldId) {
        return repository.getValues(fieldId);
    }
}
