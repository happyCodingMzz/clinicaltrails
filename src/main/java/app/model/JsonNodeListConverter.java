package app.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Converter(autoApply = true)
public class JsonNodeListConverter implements AttributeConverter<List<JsonNode>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<JsonNode> jsonNodeList) {
        if (jsonNodeList == null || jsonNodeList.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(jsonNodeList);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert List<JsonNode> to JSON", e);
        }
    }

    @Override
    public List<JsonNode> convertToEntityAttribute(String s) {
        if (!StringUtils.hasText(s) || "[]".equals(s)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(s, new TypeReference<List<JsonNode>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert JSON to List<JsonNode>", e);
        }
    }
}
