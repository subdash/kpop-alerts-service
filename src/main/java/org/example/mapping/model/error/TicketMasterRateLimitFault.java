package org.example.mapping.model.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TicketMasterRateLimitFault(
        @JsonProperty("faultstring")  String faultString,
        TicketMasterRateLimitDetail detail
) {
}
