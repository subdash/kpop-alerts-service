import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.io.Resources;
import org.example.mapping.model.success.TicketMasterEventSearchApiResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapperTest {

    @Test
    void testResponseMapping() throws IOException {
        JsonMapper jackson = JsonMapper
                .builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(StreamReadFeature.AUTO_CLOSE_SOURCE)
                .build();

        String fileContents = readResource("Events.json");
        TicketMasterEventSearchApiResponse response = jackson.readValue(fileContents, new TypeReference<>() {
        });
        assertEquals(response._embedded().events().size(), 8);
    }

    private static String readResource(final String fileName) throws IOException {
        return Resources.toString(Resources.getResource(fileName), Charset.defaultCharset());
    }
}
