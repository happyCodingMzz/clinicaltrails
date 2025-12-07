package app.service;


import app.dao.CentralContactDao;
import app.dao.ClinicalTrailDao;
import app.dao.LocationDao;
import app.model.CentralContactModule;
import app.model.ClinicalTrailModule;
import app.model.ContactsLocationsModule;
import app.model.LocationModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
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
    LocationDao locationDao;

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
        new Thread(()->doTranslation(addedData, clinicalTrailModule->
                clinicalTrailDao.save(clinicalTrailModule))).start();
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

    private void doTranslation(List<ClinicalTrailModule> clinicalTrailModules, Consumer<ClinicalTrailModule> savingTranslatedData){
        clinicalTrailModules.forEach(clinicalTrailModule ->
        {
            try {
                Thread.sleep(1000);
                clinicalTrailModule.setBriefSummary(translatorService.translate(clinicalTrailModule.getBriefSummary()));
                clinicalTrailModule.setCondition(translatorService.translate(clinicalTrailModule.getCondition()));
                clinicalTrailModule.setOfficialTitle(translatorService.translate(clinicalTrailModule.getOfficialTitle()));
                clinicalTrailModule.setTranslated(true);
                savingTranslatedData.accept(clinicalTrailModule);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        });
        log.info("Finished translation for {} clinical trail records", clinicalTrailModules.size());
    }

    public void doTranslateForField(String fieldName){
        clinicalTrailDao.findAll().forEach(clinicalTrailModule -> {
            if (clinicalTrailModule.isTranslated()){
                return;
            }
            try {
                Field field = clinicalTrailModule.getClass().getDeclaredField(fieldName);
                ReflectionUtils.makeAccessible(field);
                String value = (String) field.get(clinicalTrailModule);
                String translated = String.join("\n",translatorService.translate(value));
                if(!StringUtils.isEmpty(translated)) {
                    clinicalTrailModule.setTranslated(true);
                    ReflectionUtils.setField(field, clinicalTrailModule, translated);
                    clinicalTrailDao.save(clinicalTrailModule);
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        });
    }

}
