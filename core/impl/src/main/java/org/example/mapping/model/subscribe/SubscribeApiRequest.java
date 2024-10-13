package org.example.mapping.model.subscribe;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SubscribeApiRequest(
        String email
) {
}
