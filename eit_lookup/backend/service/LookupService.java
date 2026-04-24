package backend.service;

import backend.dto.LookupDTO;
import java.util.List;

public interface LookupService {
    void createLookup(String name, List<String> values);
    List<LookupDTO> getAll();
}