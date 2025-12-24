package app.gateway;

import app.model.ClinicalTrailVO;
import app.model.RegionMetaDataModule;
import app.service.QueryClinicalTrailsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/query")
@Tag(name = "Clinical Trails Fetching EndPoint")
@CrossOrigin(origins = "*",
        allowedHeaders = "*",
        exposedHeaders = {"X-API-KEY", "Authorization"},
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class ClinicTrailsQueryController {

    @Autowired
    QueryClinicalTrailsService queryClinicalTrailsService;

    @GetMapping("/all")
    public List<ClinicalTrailVO> queryAll(){
        return queryClinicalTrailsService.getAllTrails();
    }

    @GetMapping("/findByCountry")
    public List<ClinicalTrailVO> queryByCountry(@RequestParam String country ){
        return queryClinicalTrailsService.getByCountry(country);
    }

    @GetMapping("/findRegions")
    public List<RegionMetaDataModule> queryRegions(){
        return queryClinicalTrailsService.getRegions();
    }

}
