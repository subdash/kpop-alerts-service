package org.example.mapping.model.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TicketMasterEventSearchApiError(
        List<TicketMasterApiError> errors
) {
}
