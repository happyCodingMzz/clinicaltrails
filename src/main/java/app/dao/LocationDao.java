package app.dao;

import app.model.LocationModule;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationDao extends CrudRepository<LocationModule, String> {

    @Modifying
    @Query(value = "DELETE FROM LOCATION l WHERE l.nct_id in (:ntcs)",
            nativeQuery = true)
    void deleteAllByNctIdIn(@Param("ntcs")List<String> ntcIds);

    List<LocationModule> findByNctId(String nctId);

    List<LocationModule> findByCountry(String country);
}
