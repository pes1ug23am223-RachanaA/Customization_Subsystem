package backend.service;

import backend.dto.FieldDTO;
import backend.dto.FormDTO;
import backend.repository.IFormRepository;

import java.util.List;

public class FormServiceImpl implements FormService {

    private final IFormRepository repository;

    public FormServiceImpl(IFormRepository repository) {
        this.repository = repository;
    }

    @Override
    public int createForm(String name, String layoutType) {
        return repository.createForm(name, layoutType);
    }

    @Override
    public List<FormDTO> getAllForms() {
        return repository.getAllForms();
    }

    @Override
    public FormDTO getFormById(int formId) {
        return repository.getFormById(formId);
    }

    @Override
    public void deleteForm(int formId) {
        repository.deleteForm(formId);
    }

    @Override
    public void addField(int formId, FieldDTO field) {
        repository.addFieldToForm(formId, field);
    }

    @Override
    public List<FieldDTO> getFields(int formId) {
        return repository.getFieldsByForm(formId);
    }
}