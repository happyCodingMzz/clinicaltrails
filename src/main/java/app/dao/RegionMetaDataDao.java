package app.dao;

import app.model.RegionMetaDataModule;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegionMetaDataDao extends CrudRepository<RegionMetaDataModule, String> {

    List<RegionMetaDataModule> findByCountry(String country);

    RegionMetaDataModule findByCountryAndStateAndCity(String country, String state, String city);
}
