package backend.repository;

import backend.dto.LookupDTO;
import java.util.List;

public interface ILookupRepository {
    void save(LookupDTO lookup);
    List<LookupDTO> findAll();
}