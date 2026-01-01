package app.dao;

import app.model.ClinicalTrailModule;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ClinicalTrailDao extends CrudRepository<ClinicalTrailModule, String> {

    @NotNull
    List<ClinicalTrailModule> findAll();

    List<ClinicalTrailModule> findByNctIdIn(Set<String> nctIds);

    @Query(value = "SELECT * FROM clinical_trail ct WHERE ct.nct_id LIKE %:nctId% OR ct.official_title LIKE %:officialTitle%", nativeQuery = true)
    List<ClinicalTrailModule> findByNctIdLikeOrOfficialTitleLike(@Param("nctId") String nctId, @Param("officialTitle") String officialTitle);
}
