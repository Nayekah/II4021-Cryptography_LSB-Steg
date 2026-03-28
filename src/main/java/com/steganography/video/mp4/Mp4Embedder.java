package com.steganography.video.mp4;

import com.steganography.utils.common.Metrics;
import com.steganography.video.EmbedResult;
import com.steganography.video.Reader;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Mp4Embedder {
    public EmbedResult embedText(File input, File output, String text, String stegoKey, String encKey) throws Exception {
        return embedPayload(input, output, text.getBytes(StandardCharsets.UTF_8), false, "", stegoKey, encKey);
    }

    public EmbedResult embedFile(File input, File output, File secretFile, String stegoKey, String encKey) throws Exception {
        return embedPayload(input, output, Files.readAllBytes(secretFile.toPath()), true, secretFile.getName(), stegoKey, encKey);
    }

    public static long calculateCapacityBytes(File input) {
        return Mp4Support.calculateCapacityBytes(input.length());
    }

    private EmbedResult embedPayload(File input, File output, byte[] payload, boolean isFile, String fileName, String stegoKey, String encKey) throws Exception {
        boolean randomMode = stegoKey != null && !stegoKey.isBlank();
        Mp4Payload packed = Mp4Support.buildPayload(payload, isFile, fileName, encKey != null && !encKey.isBlank(), encKey, randomMode, stegoKey);
        byte[] sourceBytes = Files.readAllBytes(input.toPath());
        long requiredBytes = Mp4Support.BOX_HEADER_SIZE + packed.boxPayload.length;
        long availableBytes = Mp4Support.MAX_BOX_SIZE - sourceBytes.length;

        Mp4Support.validateCapacityBytes(requiredBytes, availableBytes);
        Files.write(output.toPath(), Mp4Support.attachHiddenBox(sourceBytes, packed.boxPayload));
        return buildResult(input);
    }

    private EmbedResult buildResult(File input) throws Exception {
        Reader reader = new Reader(input);
        long[][] histogram = new long[3][256];
        int[] frameCount = {0};

        reader.processFrames((BufferedImage frame, int frameIndex) -> {
            Metrics.accumulateHistogramData(frame, histogram);
            frameCount[0]++;
            return true;
        });

        int[][] averageHist = frameCount[0] == 0 ? new int[3][256] : Metrics.averageHistogramData(histogram, frameCount[0]);
        return new EmbedResult(0.0, Double.POSITIVE_INFINITY, averageHist, averageHist);
    }
}