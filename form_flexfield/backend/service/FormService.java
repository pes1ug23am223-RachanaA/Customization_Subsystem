package backend.service;

import backend.dto.FieldDTO;
import backend.dto.FormDTO;

import java.util.List;

public interface FormService {

    int createForm(String name, String layoutType);

    List<FormDTO> getAllForms();

    FormDTO getFormById(int formId);

    void deleteForm(int formId);

    void addField(int formId, FieldDTO field);

    List<FieldDTO> getFields(int formId);
}