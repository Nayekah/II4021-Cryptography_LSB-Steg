package com.steganography.video;

import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.javacpp.indexer.UByteIndexer;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.steganography.crypto.A5;

public class Extractor {

    private A5 cipher;

    public Extractor() {
        this.cipher = new A5();
    }

    public String extractText(File input, Long stegoKey, Long a5Key) throws Exception {
        byte[] payload = processExtraction(input, stegoKey, a5Key);
        return new String(payload, "UTF-8");
    }

    public void extractToFile(File input, File output, Long stegoKey, Long a5Key) throws Exception {
        byte[] payload = processExtraction(input, stegoKey, a5Key);
        Files.write(output.toPath(), payload);
    }

    private byte extractByte(UByteIndexer indexer, int width, int pixelIndex) {
        long y = pixelIndex / width;
        long x = pixelIndex % width;

        int b = indexer.get(y, x, 0);
        int g = indexer.get(y, x, 1);
        int r = indexer.get(y, x, 2);

        int valB = b & 3;
        int valG = (g & 7) << 2;
        int valR = (r & 7) << 5;

        return (byte)(valB | valG | valR);
    }

    private byte[] processExtraction(File input, Long stegoKey, Long a5Key) throws Exception {
        byte[] payload = null;

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(input)) {
            grabber.start();

            int width = grabber.getImageWidth();
            int height = grabber.getImageHeight();
            int pixelsPerFrame = width * height;

            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
            Frame frame;

            int payloadIndex = 0;
            int payloadLen = -1;
            boolean headerDone = false;
            int frameIndex = 0;
            Random randomizer = ((stegoKey != null) ? new Random(stegoKey) : null);

            while ((frame = grabber.grabImage()) != null) {
                Mat mat = converter.convertToMat(frame);

                if (mat != null) {
                    UByteIndexer indexer = mat.createIndexer();

                    if (!headerDone) {
                        int length = 0;
                        for (int i = 0; i < 4; i++) {
                            byte sizeByte = extractByte(indexer, width, i);
                            length = (length << 8) | (sizeByte & 255);
                        }

                        payloadLen = length;
                        headerDone = true;

                        if (payloadLen <= 0 || payloadLen > grabber.getLengthInVideoFrames() * pixelsPerFrame) {
                            throw new IllegalStateException("Headernya rusak :(");
                        }

                        payload = new byte[payloadLen];
                    }

                    List<Integer> pixelList = new ArrayList<>(pixelsPerFrame);
                    int start = (frameIndex == 0) ? 4 : 0;

                    for (int i = start; i < pixelsPerFrame; i++) {
                        pixelList.add(i);
                    }

                    if (randomizer != null) {
                        Collections.shuffle(pixelList, randomizer);
                    }

                    int pixelIndex = 0;
                    while (payloadIndex < payloadLen && pixelIndex < pixelList.size()) {
                        int pos = pixelList.get(pixelIndex);
                        payload[payloadIndex] = extractByte(indexer, width, pos);

                        payloadIndex++;
                        pixelIndex++;
                    }

                    mat.release();

                    if (payloadIndex >= payloadLen) {
                        break;
                    }
                }

                frameIndex++;
            }

            if (payloadIndex < payloadLen) {
                throw new RuntimeException("Videonya kurang panjang");
            }
        }

        if (a5Key != null && payload != null) {
            payload = cipher.decrypt(payload, a5Key, 0);
        }

        return payload;
    }
}