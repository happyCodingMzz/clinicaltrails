package app.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class ClinicalTrailDetailVO {

    String city;

    String country;

    String state;

    String facility;

    String contactName;

    String contactEmail;

    String contactPhone;

    String status;
}
