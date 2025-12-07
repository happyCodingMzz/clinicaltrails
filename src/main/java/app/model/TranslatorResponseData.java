package app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TranslatorResponseData {

    @JsonProperty("from")
    String from;

    @JsonProperty("to")
    String to;

    @JsonProperty("trans_result")
    private List<TransResult> transResult;

    @JsonProperty("error_code")
    private String errorCode;

    @JsonProperty("error_msg")
    private String errorMsg;

    @Data
    public static class TransResult {
        @JsonProperty("src")
        private String src;
        @JsonProperty("dst")
        private String dst;
    }
}
