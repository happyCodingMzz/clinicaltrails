package app.service;

import app.dao.ClinicalTrailDao;
import app.dao.LocationDao;
import app.dao.RegionMetaDataDao;
import app.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class QueryClinicalTrailsService {

    @Autowired
    ClinicalTrailDao clinicalTrailDao;

    @Autowired
    LocationDao locationDao;

    @Autowired
    RegionMetaDataDao regionMetaDataDao;

    public List<ClinicalTrailVO> getAllTrails(){
        List<ClinicalTrailVO> result = new ArrayList<>();
        clinicalTrailDao.findAll().forEach(clinicalTrailModule -> {
            List<LocationModule> locationModules = locationDao.findByNctId(clinicalTrailModule.getNctId());
            List<ClinicalTrailDetailVO> detailVOList = new ArrayList<>();
            locationModules.forEach(locationModule -> {
                JsonNode contactNode = Optional.ofNullable(locationModule.getContacts())
                                .flatMap(jsonNodes -> jsonNodes.stream().findFirst()).orElse(null);

                detailVOList.add(ClinicalTrailDetailVO.builder()
                                .city(locationModule.getCity())
                                .country(locationModule.getCountry())
                                .state(locationModule.getState())
                                .facility(locationModule.getFacility())
                                .contactEmail(contactNode!=null?contactNode.get("email").textValue():"")
                                .contactName(contactNode!=null?contactNode.get("name").textValue():"")
                                .contactPhone(contactNode!=null?contactNode.get("phone").textValue():"")
                                .status(locationModule.getStatus()).build());
            });
            result.add(ClinicalTrailVO.builder().nctId(clinicalTrailModule.getNctId())
                            .condition(clinicalTrailModule.getCondition())
                            .briefSummary(clinicalTrailModule.getBriefSummary())
                            .eligibilityCriteria(clinicalTrailModule.getEligibilityCriteria())
                            .officialTitle(clinicalTrailModule.getOfficialTitle())
                            .detailVOList(detailVOList).build());
        });
        return  result;
    }

    public  List<ClinicalTrailVO> getByCountry(String country){
        List<ClinicalTrailVO> result = new ArrayList<>();
        Map<String, List<LocationModule>> locationMap = locationDao.findByCountry(country).stream().collect(Collectors.groupingBy(LocationModule::getNctId));
        Set<String> nctIds = locationMap.keySet();
        List<ClinicalTrailModule> clinicalTrailModules = clinicalTrailDao.findByNctIdIn(nctIds);
        clinicalTrailModules.forEach(clinicalTrailModule -> {
            List<ClinicalTrailDetailVO> detailVOList = new ArrayList<>();
            locationMap.get(clinicalTrailModule.getNctId()).forEach(locationModule -> {
                if(!Objects.equals(locationModule.getCountry(), country)) return;
                JsonNode contactNode = Optional.ofNullable(locationModule.getContacts())
                        .flatMap(jsonNodes -> jsonNodes.stream().findFirst()).orElse(null);

                detailVOList.add(ClinicalTrailDetailVO.builder()
                        .city(locationModule.getCity())
                        .country(locationModule.getCountry())
                        .state(locationModule.getState())
                        .facility(locationModule.getFacility())
                        .contactEmail(contactNode!=null?contactNode.get("email").textValue():"")
                        .contactName(contactNode!=null?contactNode.get("name").textValue():"")
                        .contactPhone(contactNode!=null?contactNode.get("phone").textValue():"")
                        .status(locationModule.getStatus()).build());
            });
            result.add(ClinicalTrailVO.builder().nctId(clinicalTrailModule.getNctId())
                    .condition(clinicalTrailModule.getCondition())
                    .briefSummary(clinicalTrailModule.getBriefSummary())
                    .eligibilityCriteria(clinicalTrailModule.getEligibilityCriteria())
                    .officialTitle(clinicalTrailModule.getOfficialTitle())
                    .detailVOList(detailVOList).build());
        });

        return result;
    }

    public List<RegionMetaDataModule> getRegions(){
        return StreamSupport.stream(regionMetaDataDao.findAll().spliterator(), true)
                .collect(Collectors.toList());
    }
}
