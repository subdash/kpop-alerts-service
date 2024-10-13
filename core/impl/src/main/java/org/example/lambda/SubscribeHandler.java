package org.example.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.example.mapping.model.subscribe.SubscribeApiRequest;
import org.example.mapping.model.subscribe.SubscribeApiResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sns.model.SubscribeResponse;

import java.util.HashMap;
import java.util.Map;

public class SubscribeHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String SNS_TOPIC_ARN = System.getenv("SNS_TOPIC_ARN");
    private static final JsonMapper jackson = JsonMapper
            .builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(StreamReadFeature.AUTO_CLOSE_SOURCE)
            .build();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        final SubscribeApiRequest apiRequest;
        try {
            context.getLogger().log("Raw body: " + input.getBody());
            apiRequest = jackson.readValue(input.getBody(), SubscribeApiRequest.class);
        } catch (JsonProcessingException e) {
            context.getLogger().log(String.format("Error deserializing request: %s", e.getMessage()));
            return getErrorResponse(new SubscribeApiResponse(false, e.getMessage()), 400);
        }

        try (SnsClient snsClient = SnsClient.builder()
                .region(Region.US_EAST_1)
                .build()) {
            final SubscribeRequest snsRequest = SubscribeRequest
                    .builder()
                    .topicArn(SNS_TOPIC_ARN)
                    .endpoint(apiRequest.email())
                    .protocol("email")
                    .returnSubscriptionArn(true)
                    .build();
            final SubscribeResponse snsResponse = snsClient.subscribe(snsRequest);
            context.getLogger().log(String.format("SNS subscription ARN: %s", snsResponse.subscriptionArn()));
            return getSuccessResponse();
        } catch (Exception e) {
            context.getLogger().log(String.format("Error subscribing user %s: %s", apiRequest.email(), e.getMessage()));
            return getErrorResponse(new SubscribeApiResponse(false, e.getMessage()), 500);
        }
    }

    private static Map<String, String> getProxyHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Methods", "*");
        headers.put("Access-Control-Allow-Headers", "*");
        headers.put("Access-Control-Allow-Origin", "*");
        return headers;
    }

    private static APIGatewayProxyResponseEvent getSuccessResponse() {
        final SubscribeApiResponse responseBody = new SubscribeApiResponse(true, null);
        String responseJsonString;
        try {
            responseJsonString = jackson.writeValueAsString(responseBody);
        } catch (JsonProcessingException e) {
            responseJsonString = "{ \"created\": true }";
        }

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(201)
                .withHeaders(getProxyHeaders())
                .withBody(responseJsonString)
                .withIsBase64Encoded(Boolean.FALSE);
    }

    private static APIGatewayProxyResponseEvent getErrorResponse(SubscribeApiResponse responseBody, int statusCode) {
        String responseJsonString;
        try {
            responseJsonString = jackson.writeValueAsString(responseBody);
        } catch (JsonProcessingException e) {
            responseJsonString = "{}";
        }

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(getProxyHeaders())
                .withBody(responseJsonString)
                .withIsBase64Encoded(Boolean.FALSE);
    }
}
