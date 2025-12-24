package app.model;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name="location")
public class LocationModule {

    @Id
    String id;

    String nctId;
    String facility;

    String status;

    String city;

    String state;

    String zip;

    String country;

    boolean isTranslated;


    @Column(name = "contacts", columnDefinition = "json")
    @Convert(converter = JsonNodeListConverter.class)
    List<JsonNode> contacts;
}
