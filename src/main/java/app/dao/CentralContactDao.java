package app.dao;

import app.model.CentralContactModule;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CentralContactDao extends CrudRepository<CentralContactModule, String> {

    void deleteAllByNctIdIn(List<String> ntcIds);
}
