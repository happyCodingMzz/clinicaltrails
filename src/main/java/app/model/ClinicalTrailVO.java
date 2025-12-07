package app.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class ClinicalTrailVO {

    private String nctId;

    private String condition;

    private String officialTitle;

    private String briefSummary;

    private String eligibilityCriteria;

    private List<ClinicalTrailDetailVO> detailVOList;
}
