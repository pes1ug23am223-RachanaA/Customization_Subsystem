package backend.service;

import backend.dto.LookupDTO;
import backend.repository.ILookupRepository;

import java.util.List;

public class LookupServiceImpl implements LookupService {

    private ILookupRepository repo;

    public LookupServiceImpl(ILookupRepository repo) {
        this.repo = repo;
    }

    public void createLookup(String name, List<String> values) {
        repo.save(new LookupDTO(name, values));
    }

    public List<LookupDTO> getAll() {
        return repo.findAll();
    }
}