package app.service;


import app.dao.CentralContactDao;
import app.dao.ClinicalTrailDao;
import app.dao.LocationDao;
import app.dao.RegionMetaDataDao;
import app.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class LoadClinicalTrailsDataService {

    //TODO: configurable
    static final String URL = "https://clinicaltrials.gov/api/v2/studies?format=json&query.cond=Lung+Cancer&query.locn=AREA%5BLocationCountry%5DChina&filter.overallStatus=RECRUITING&countTotal=true&pageSize=1000";

    @Autowired
    OkHttpClient okHttpClient;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TranslatorService translatorService;

    @Autowired
    ClinicalTrailDao clinicalTrailDao;

    @Autowired
    CentralContactDao centralContactDao;

    @Autowired
    RegionMetaDataDao regionMetaDataDao;

    @Autowired
    LocationDao locationDao;

    Map<String, CrudRepository> repositoryMap;

    private static final String CHINA = "中国";

    @PostConstruct
    public void init(){
        repositoryMap = Stream.of(
                new AbstractMap.SimpleEntry<>("ClinicalTrailModule",clinicalTrailDao ),
                new AbstractMap.SimpleEntry<>("LocationModule", locationDao )
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public List<ClinicalTrailModule> loadingLatestData(){
        log.info("Loading latest Clinical Trail data beginning");
        List<ClinicalTrailModule> results = new ArrayList<>();
        try {
            String response = fetchingDataFromSource(URL).string();
            results = convertToObj(response).stream().filter(this::validateClinicalTrailData).collect(Collectors.toList());
        }
        catch (IOException e){
            log.error("Error happened when loading clinical trials data", e);
        }
        log.info("Getting {} validate clinical trails", results.size());
        return results;
    }

    private ResponseBody fetchingDataFromSource(String url) throws IOException {
        log.info("Fetching Data ...");
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent"," Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36")
                .build();
        return okHttpClient.newCall(request).execute().body();
    }

    /**
     * refresh Clinical Trail data in DB as a scheduled job
     * 1. remove all the records in DB which is not found in retrieved data set (by nctId)
     * 2. insert new added data set into DB
     */
    @Scheduled(cron = "0 0 12 * * ?")
    @Transactional
    public void refreshClinicalTrailData(){
        List<ClinicalTrailModule> loadedClinicalTrailData = loadingLatestData();
        List<ClinicalTrailModule> existClinicalTrailData = StreamSupport.stream(clinicalTrailDao.findAll().spliterator(),false).collect(Collectors.toList());
        List<ClinicalTrailModule> outdatedClinicalTrailData = existClinicalTrailData.stream().filter(data ->
                !loadedClinicalTrailData.stream().map(ClinicalTrailModule::getNctId).collect(Collectors.toList()).contains(data.getNctId())
        ).collect(Collectors.toList());
        List<ClinicalTrailModule> addedData = loadedClinicalTrailData.stream().filter(data ->
                !existClinicalTrailData.stream().map(ClinicalTrailModule::getNctId).collect(Collectors.toList()).contains(data.getNctId())
        ).collect(Collectors.toList());
        List<ContactsLocationsModule> addedContactsLocations = addedData.stream().map(ClinicalTrailModule::getContactsLocations).collect(Collectors.toList());
        List<CentralContactModule> addedCentralContacts = addedContactsLocations.stream().map(ContactsLocationsModule::getCentralContactModules).flatMap(List::stream).collect(Collectors.toList());
        List<LocationModule> addedLocations = addedContactsLocations.stream().map(ContactsLocationsModule::getLocationModules).flatMap(List::stream).collect(Collectors.toList());
        outdatedClinicalTrailData.forEach(clinicalTrailModule -> clinicalTrailModule.setValid(false));
        clinicalTrailDao.deleteAll(outdatedClinicalTrailData);

        clinicalTrailDao.saveAll(addedData);
        centralContactDao.saveAll(addedCentralContacts);
        addedLocations.forEach(locationDao::save);
        new Thread(()-> {
            doTranslationForTrails(addedData, clinicalTrailModule->
                    clinicalTrailDao.save(clinicalTrailModule));
            doTranslationForLocations(addedLocations,
                    (locationModule, regionMetaDataModule) -> {
                        locationDao.save(locationModule);
                        regionMetaDataDao.save(regionMetaDataModule);
                    });
        }).start();
        log.info("Refresh Clinical Trail Data successfully");
    }

    private List<ClinicalTrailModule> convertToObj(String str){
        List<ClinicalTrailModule> results = new ArrayList<>();
        try {
            ArrayNode studyArray = (ArrayNode) objectMapper.readTree(str).get("studies");
            studyArray.forEach(node -> results.add(objectMapper.convertValue(node, ClinicalTrailModule.class)));
        } catch (JsonProcessingException e) {
            log.error("Error happened when parsing clinical trail data", e);
        }
        return results;
    }

    private boolean validateClinicalTrailData(ClinicalTrailModule clinicalTrailModule){
        return clinicalTrailModule.getContactsLocations()!=null;
    }

    private void doTranslationForTrails(List<ClinicalTrailModule> clinicalTrailModules, Consumer<ClinicalTrailModule> savingTranslatedData){
        clinicalTrailModules.forEach(clinicalTrailModule ->
        {
            try {
                Thread.sleep(500);
                String translatedSummary = translatorService.translate(clinicalTrailModule.getBriefSummary());
                String translatedCondition = translatorService.translate(clinicalTrailModule.getCondition());
                String translatedTitle = translatorService.translate(clinicalTrailModule.getOfficialTitle());
                String translatedCriteria = translatorService.translate(clinicalTrailModule.getEligibilityCriteria());
                if(!StringUtils.isEmpty(translatedSummary))
                    clinicalTrailModule.setBriefSummary(translatedSummary);
                if(!StringUtils.isEmpty(translatedCondition))
                    clinicalTrailModule.setCondition(translatedCondition);
                if(!StringUtils.isEmpty(translatedTitle))
                    clinicalTrailModule.setOfficialTitle(translatedTitle);
                if(!StringUtils.isEmpty(translatedCriteria))
                    clinicalTrailModule.setEligibilityCriteria(translatedCriteria);

                if(!StringUtils.isEmpty(translatedSummary) &&
                        !StringUtils.isEmpty(translatedCondition) &&
                        !StringUtils.isEmpty(translatedTitle) &&
                        !StringUtils.isEmpty(translatedCriteria))
                    clinicalTrailModule.setTranslated(true);

                savingTranslatedData.accept(clinicalTrailModule);

            } catch (InterruptedException e) {
                log.error("Error happened when translating ClinicTrails ", e);
                Thread.currentThread().interrupt();
            }
        });
        log.info("Finished translation for {} clinical trail records", clinicalTrailModules.size());
    }

    private void doTranslationForLocations(List<LocationModule> locationModules, BiConsumer<LocationModule, RegionMetaDataModule> savingTranslatedData){
        locationModules.forEach(locationModule ->
        {
            try{
                Thread.sleep(500);
                String translatedCountry = translatorService.translate(locationModule.getCountry());
                String translatedState = translatorService.translate(locationModule.getState());
                String translatedCity = translatorService.translate(locationModule.getCity());
                String translatedFacility = translatorService.translate(locationModule.getFacility());
                if(!StringUtils.isEmpty(translatedCountry))
                    locationModule.setCountry(translatedCountry);
                if(!StringUtils.isEmpty(translatedState))
                    locationModule.setState(translatedState);
                if(!StringUtils.isEmpty(translatedCity))
                    locationModule.setCity(translatedCity);
                if(!StringUtils.isEmpty(translatedFacility))
                    locationModule.setFacility(translatedFacility);

                RegionMetaDataModule regionMetaDataModule = null;
                if(!StringUtils.isEmpty(translatedCountry) &&
                        !StringUtils.isEmpty(translatedState) &&
                        !StringUtils.isEmpty(translatedCity)) {
                    if(CHINA.equals(translatedCountry) && (regionMetaDataDao.findByCountryAndStateAndCity(translatedCountry,
                                translatedState, translatedCity) == null)){
                            regionMetaDataModule = new RegionMetaDataModule(UUID.randomUUID().toString(),
                                    translatedCountry, translatedState, translatedCity);
                    }
                    if(!StringUtils.isEmpty(translatedFacility))
                        locationModule.setTranslated(true);
                }
                savingTranslatedData.accept(locationModule, regionMetaDataModule);
            }
            catch (InterruptedException e) {
                log.error("Error happened when translating Locations", e);
                Thread.currentThread().interrupt();
            }
        });
    }

    public void doTranslateForTable(String tableName, String fieldName){
        repositoryMap.get(tableName).findAll().forEach(object -> {
            try {
                Field field = object.getClass().getDeclaredField(fieldName);
                ReflectionUtils.makeAccessible(field);
                String value = (String) field.get(object);
                String translated = String.join("\n",translatorService.translate(value));
                if(!StringUtils.isEmpty(translated)){
                    ReflectionUtils.setField(field, object, translated);
                    repositoryMap.get(tableName).save(object);
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        });
    }

}
