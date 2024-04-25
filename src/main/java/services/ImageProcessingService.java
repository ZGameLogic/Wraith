package services;

import data.discord.SeaOfThievesIslandData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.MatOfByte;

@Service
@Slf4j
public class ImageProcessingService {
    private final static String SOT_ISLAND_ASSETS_DIR = "assets\\sot-islands";
    private final List<SeaOfThievesIslandData> islands;
    private final SeaOfThievesIslandData noIsland;

    public ImageProcessingService() throws IOException {
        this.noIsland = new SeaOfThievesIslandData(new ClassPathResource(SOT_ISLAND_ASSETS_DIR + "\\unknown.png").getFile(), "Unknown", "??");
        this.islands = List.of();
    }

    private Mat readImageFromStream(InputStream inputStream) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            // TODO this while loop is broken
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            byte[] data = byteArrayOutputStream.toByteArray();
            MatOfByte matOfByte = new MatOfByte(data);

            return Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_GRAYSCALE);
        } catch (Exception e) {
            log.error("Unable to encode image", e);
            return new Mat();
        }
    }

    public SeaOfThievesIslandData getSOTIslandFromPng(InputStream inputImage){
//        Mat inputImageMat = readImageFromStream(inputImage);
        for(SeaOfThievesIslandData island: islands){

        }
        return noIsland;
    }
}
