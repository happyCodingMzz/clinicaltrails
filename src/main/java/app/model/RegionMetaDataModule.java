package app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@NoArgsConstructor
@AllArgsConstructor
@Table(name="regionMetadata")
@Entity
@Getter
public class RegionMetaDataModule {
    @Id
    private String id;

    String country;

    String state;

    String city;


}
