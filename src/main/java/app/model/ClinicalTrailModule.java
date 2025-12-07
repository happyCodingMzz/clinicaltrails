package app.model;

import app.service.decode.ClinicalTrailDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@JsonDeserialize(using = ClinicalTrailDeserializer.class)
@Table(name="clinicalTrail")
public class ClinicalTrailModule {

    @Id
    @JsonProperty("NCTId")
    String nctId;

    @Lob
    @JsonProperty("OfficialTitle")
    String officialTitle;

    @JsonProperty("Condition")
    @Column(name = "`condition`")
    String condition;


    @Lob
    @JsonProperty("BriefSummary")
    @Basic(fetch = FetchType.LAZY)
    String briefSummary;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @JsonProperty("EligibilityCriteria")
    String eligibilityCriteria;

    @Transient
    @JsonProperty("ContactsLocations")
    ContactsLocationsModule contactsLocations;

    @Column(name = "isTranslated")
    boolean translated;

    @Column(name = "isValid")
    boolean valid;

}
