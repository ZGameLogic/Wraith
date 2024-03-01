package data.api.GPT.payloads;

import lombok.Data;

@Data
public class ImagePayload {
    private final String model = "dall-e-3";
    private final String size = "1024x1024";
    private final String quality = "hd";
    private final String prompt;

}
