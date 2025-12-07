package app.dao;

import app.model.ClinicalTrailModule;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClinicalTrailDao extends CrudRepository<ClinicalTrailModule, String> {

    @NotNull
    List<ClinicalTrailModule> findAll();
}
