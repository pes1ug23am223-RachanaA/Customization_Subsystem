package backend.repository;

import backend.dto.EITDTO;
import java.sql.SQLException;
import java.util.List;

public interface IEITRepository {
    void save(EITDTO eit);
    List<EITDTO> findAll();
    void deleteById(int fieldId) throws SQLException;
    void deleteByName(String name) throws SQLException;
}