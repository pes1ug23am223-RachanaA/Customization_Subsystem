package backend.service;

import backend.dto.EITDTO;
import backend.repository.IEITRepository;
import java.sql.SQLException;
import java.util.List;

public class EITServiceImpl implements EITService {

    private IEITRepository repo;

    public EITServiceImpl(IEITRepository repo) {
        this.repo = repo;
    }

    public void createEIT(String name, String type) {
        repo.save(new EITDTO(name, type));
    }

    public List<EITDTO> getAll() {
        return repo.findAll();
    }

    @Override
    public void deleteEIT(int fieldId) throws SQLException {
        repo.deleteById(fieldId);
    }

    @Override
    public void deleteEITByName(String name) throws SQLException {
        repo.deleteByName(name);
    }
}