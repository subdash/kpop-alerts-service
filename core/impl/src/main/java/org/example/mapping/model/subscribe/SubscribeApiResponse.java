package org.example.mapping.model.subscribe;

import javax.annotation.Nullable;

public record SubscribeApiResponse(
        boolean created,
        @Nullable String error
) {
}
