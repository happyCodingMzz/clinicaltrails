package app.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Setter
@Getter
@Table(name="centralContact")
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class CentralContactModule {

    @Id
    String id;

    String nctId;

    String name;

    String role;

    String phone;

    String email;
}
