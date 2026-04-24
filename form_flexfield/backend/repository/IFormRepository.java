package backend.repository;

import backend.dto.FormDTO;
import backend.dto.FieldDTO;

import java.util.List;

public interface IFormRepository {

    int createForm(String name, String layoutType);

    FormDTO getFormById(int formId);

    List<FormDTO> getAllForms();

    void updateForm(int formId, String newName);

    void deleteForm(int formId);

    void addFieldToForm(int formId, FieldDTO field);

    void removeFieldFromForm(int formId, int fieldId);

    List<FieldDTO> getFieldsByForm(int formId);
}