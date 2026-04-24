package backend.service;

import backend.dto.EITDTO;
import java.sql.SQLException;
import java.util.List;

public interface EITService {
    void createEIT(String name, String type);
    List<EITDTO> getAll();
    void deleteEIT(int fieldId) throws SQLException;
    void deleteEITByName(String name) throws SQLException;
}