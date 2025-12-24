package app.gateway;

import app.model.ClinicalTrailModule;
import app.service.LoadClinicalTrailsDataService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Data Management API")
public class DataManagementController {

    @Autowired
    private LoadClinicalTrailsDataService loadClinicalTrailsDataService;

    @PostMapping("/translation")
    public void fieldTranslate(@Parameter String tableName, @Parameter String field){
        loadClinicalTrailsDataService.doTranslateForTable(tableName,field);
    }


    @GetMapping("/allTrailsFromSource")
    public List<ClinicalTrailModule> getAllTrails(){
        return loadClinicalTrailsDataService.loadingLatestData();
    }

    @PostMapping("/refreshTrailsFromSource")
    public void refreshTrails(){
        loadClinicalTrailsDataService.refreshClinicalTrailData();
    }
}
