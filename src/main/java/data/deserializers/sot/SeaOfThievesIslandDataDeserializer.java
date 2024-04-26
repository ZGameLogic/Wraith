package data.deserializers.sot;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import data.discord.SeaOfThievesIslandData;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

import static services.ImageProcessingService.SOT_ISLAND_ASSETS_DIR;

public class SeaOfThievesIslandDataDeserializer extends JsonDeserializer<SeaOfThievesIslandData> {
    @Override
    public SeaOfThievesIslandData deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonNode json = jsonParser.getCodec().readTree(jsonParser);
        String name = json.get("name").asText();
        String region = json.get("region").asText();
        File islandPng = new ClassPathResource(SOT_ISLAND_ASSETS_DIR + "\\islands\\" + json.get("island png name").asText()).getFile();
        File areaPng = new ClassPathResource(SOT_ISLAND_ASSETS_DIR + "\\icons\\" + json.get("area png name").asText()).getFile();
        String chords = json.get("grid").asText();
        return new SeaOfThievesIslandData(islandPng, name, areaPng, region, chords);
    }
}
