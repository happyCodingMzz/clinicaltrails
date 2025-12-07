package app.service;

import app.dao.ClinicalTrailDao;
import app.dao.LocationDao;
import app.model.ClinicalTrailDetailVO;
import app.model.ClinicalTrailVO;
import app.model.LocationModule;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class QueryClinicalTrailsService {

    @Autowired
    ClinicalTrailDao clinicalTrailDao;

    @Autowired
    LocationDao locationDao;

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
}
