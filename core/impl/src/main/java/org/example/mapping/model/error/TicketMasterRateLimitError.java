package org.example.mapping.model.error;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TicketMasterRateLimitError(
        TicketMasterRateLimitFault fault
) {
}
