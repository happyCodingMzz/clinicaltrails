package app.service.decode;

import app.model.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import static app.constant.TrailFieldConstants.*;

@Slf4j
public class ClinicalTrailDeserializer extends StdDeserializer<ClinicalTrailModule> {

    public ClinicalTrailDeserializer(){
        this(null);
    }

    protected ClinicalTrailDeserializer(Class<?> vc) {
        super(vc);
    }

    @SneakyThrows
    @Override
    public ClinicalTrailModule deserialize(JsonParser jsonParser, DeserializationContext deserializationContext){
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String nctId = node.get(PROTOCOL_SECTION).get(IDENTIFICATION_MODULE).get(NCTID).asText();
        String condition = node.get(PROTOCOL_SECTION).get(CONDITIONS_MODULE).get(CONDITIONS).get(0).asText();
        String officialTitle = node.get(PROTOCOL_SECTION).get(IDENTIFICATION_MODULE).get(OFFICIAL_TITLE).asText();
        String eligibilityCriteria = node.get(PROTOCOL_SECTION).get(ELIGIBILITY_MODULE).get(ELIGIBILITY_CRITERIA).asText();
        String briefSummary = node.get(PROTOCOL_SECTION).get(DESCRIPTION_MODULE).get(BRIEF_SUMMARY).asText();
        ArrayNode centralLocationNodes = (ArrayNode) node.get(PROTOCOL_SECTION).get(CONTACTS_LOCATIONS_MODULE).get(CENTRAL_CONTACT_MODULE);
        ArrayNode locationsNodes = (ArrayNode) node.get(PROTOCOL_SECTION).get(CONTACTS_LOCATIONS_MODULE).get(LOCATION_MODULE);
        List<CentralContactModule> centralContactModules = new ArrayList<>();
        List<LocationModule> locationModules = new ArrayList<>();
        if(centralLocationNodes != null && !centralLocationNodes.isEmpty()) {
            centralLocationNodes.forEach(centralLocationNode -> {
                String name = Optional.ofNullable(centralLocationNode.get("name")).map(JsonNode::asText).orElse("");
                String role = Optional.ofNullable(centralLocationNode.get("role")).map(JsonNode::asText).orElse("");
                String phone = Optional.ofNullable(centralLocationNode.get("phone")).map(JsonNode::asText).orElse("");
                String email = Optional.ofNullable(centralLocationNode.get("email")).map(JsonNode::asText).orElse("");
                centralContactModules.add(new CentralContactModule(UUID.randomUUID().toString(), nctId, name, role, phone, email));
            });
        }
        if(locationsNodes != null && !locationsNodes.isEmpty()){
            locationsNodes.forEach(locationsNode ->{
                String facility = Optional.ofNullable(locationsNode.get("facility")).map(JsonNode::asText).orElse("");
                String status = Optional.ofNullable(locationsNode.get("status")).map(JsonNode::asText).orElse("");
                String city = Optional.ofNullable(locationsNode.get("city")).map(JsonNode::asText).orElse("");
                String state = Optional.ofNullable(locationsNode.get("state")).map(JsonNode::asText).orElse("");
                String zip = Optional.ofNullable(locationsNode.get("zip")).map(JsonNode::asText).orElse("");
                String country = Optional.ofNullable(locationsNode.get("country")).map(JsonNode::asText).orElse("");
                List<ContactModule> contactModules = new ArrayList<>();
                ArrayNode contactModuleNodes = (ArrayNode) locationsNode.get("contacts");
                if( contactModuleNodes != null && !contactModuleNodes.isEmpty()){
                    contactModuleNodes.forEach(contactModuleNode ->{
                        String contactName = Optional.ofNullable(contactModuleNode.get("name")).map(JsonNode::asText).orElse("");
                        String contactRole = Optional.ofNullable(contactModuleNode.get("role")).map(JsonNode::asText).orElse("");
                        String contactPhone = Optional.ofNullable(contactModuleNode.get("phone")).map(JsonNode::asText).orElse("");
                        String contactPhoneExt = Optional.ofNullable(contactModuleNode.get("phoneExt")).map(JsonNode::asText).orElse("");
                        String contactEmail = Optional.ofNullable(contactModuleNode.get("email")).map(JsonNode::asText).orElse("");
                        contactModules.add(new ContactModule(contactName, contactRole, contactPhone, contactPhoneExt, contactEmail));
                    });
                }
                locationModules.add(new LocationModule(UUID.randomUUID().toString(), nctId, facility, status, city, state, zip, country, contactModules.stream().map(contactModule ->
                        (JsonNode) new ObjectMapper().valueToTree(contactModule)
                ).collect(Collectors.toList())));
            });
        }
        ContactsLocationsModule contactsLocationsModule = new ContactsLocationsModule(centralContactModules, locationModules);
        return new ClinicalTrailModule(nctId, officialTitle, condition,
                briefSummary, eligibilityCriteria, contactsLocationsModule, false, true);
    }

}
