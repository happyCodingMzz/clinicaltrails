package app.dao;

import app.model.ClinicalTrailModule;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ClinicalTrailDao extends CrudRepository<ClinicalTrailModule, String> {

    @NotNull
    List<ClinicalTrailModule> findAll();

    List<ClinicalTrailModule> findByNctIdIn(Set<String> nctIds);
}
