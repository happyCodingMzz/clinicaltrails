package app.model;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.List;


@Value
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class ContactsLocationsModule {

    List<CentralContactModule> centralContactModules;

    List<LocationModule> locationModules;
 }
