package com.steganography;

import com.steganography.video.Embedder;
import com.steganography.video.Extractor;

import org.bytedeco.ffmpeg.global.avutil;

import java.io.File;
import java.io.FileWriter;

public class Main {

    public static void main(String[] args) {
        avutil.av_log_set_level(avutil.AV_LOG_QUIET);

        File dataDir = new File("./data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        File inputVideo = new File(dataDir, "input.avi");
        File stegoVideo = new File(dataDir, "stego.avi");
        File secretFile = new File(dataDir, "secret.txt");
        File outputFile = new File(dataDir, "extracted.txt");

        Long stegoKey = 123456789L;
        Long a5Key = 987654321L;

        if (!inputVideo.exists()) {
            System.err.println("Videonya belum ada bro");
            return;
        }

        try {

            Embedder embedder = new Embedder();
            Extractor extractor = new Extractor();

            long startEmbed = System.currentTimeMillis();

            embedder.embedFile(inputVideo, stegoVideo, secretFile, stegoKey, a5Key);

            long endEmbed = System.currentTimeMillis();

            System.out.println("Embed kelar dalam " + (endEmbed - startEmbed) + " ms");
            System.out.println("Output video: " + stegoVideo.getPath());

            long startExtract = System.currentTimeMillis();

            String message = extractor.extractText(stegoVideo, stegoKey, a5Key);
            extractor.extractToFile(stegoVideo, outputFile, stegoKey, a5Key);

            long endExtract = System.currentTimeMillis();

            System.out.println("Extract kelar dalam " + (endExtract - startExtract) + " ms");
            System.out.println("Pesan hasil: " + message);
            System.out.println("Output file: " + outputFile.getPath());

        } catch (Exception e) {
            System.err.println("error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}