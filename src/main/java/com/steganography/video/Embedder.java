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

public class Embedder {

    private A5 cipher;

    public Embedder() {
        this.cipher = new A5();
    }

    public void embedText(File input, File output, String text, Long stegoKey, Long a5Key) throws Exception {
        byte[] payload = text.getBytes("UTF-8");
        processEmbedding(input, output, payload, stegoKey, a5Key);
    }

    public void embedFile(File input, File output, File textFile, Long stegoKey, Long a5Key) throws Exception {
        byte[] payload = Files.readAllBytes(textFile.toPath());
        processEmbedding(input, output, payload, stegoKey, a5Key);
    }

    private void embedByte(UByteIndexer indexer, int width, int pixelIndex, byte value) {
        long y = pixelIndex / width;
        long x = pixelIndex % width;

        int b = indexer.get(y, x, 0);
        int g = indexer.get(y, x, 1);
        int r = indexer.get(y, x, 2);

        b = (b & 252) | (value & 3);
        g = (g & 248) | ((value >> 2) & 7);
        r = (r & 248) | ((value >> 5) & 7);

        indexer.put(y, x, 0, b);
        indexer.put(y, x, 1, g);
        indexer.put(y, x, 2, r);
    }

    private void processEmbedding(File input, File output, byte[] payload, Long stegoKey, Long a5Key) throws Exception {
        if (a5Key != null) {
            payload = cipher.encrypt(payload, a5Key, 0);
        }

        int payloadLen = payload.length;
        int totalBytes = payloadLen + 4;

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(input)) {
            grabber.start();

            int width = grabber.getImageWidth();
            int height = grabber.getImageHeight();
            int pixelsPerFrame = width * height;
            long totalFrames = grabber.getLengthInVideoFrames();

            if (totalFrames > 0 && totalBytes > totalFrames * pixelsPerFrame) {
                throw new IllegalArgumentException("Kegedean bos");
            }

            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(output, width, height)) {
                recorder.setVideoCodec(org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_HUFFYUV);
                recorder.setFormat("avi");
                recorder.setFrameRate(grabber.getFrameRate());
                recorder.start();

                OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
                Frame frame;

                int payloadIndex = 0;
                boolean headerDone = false;
                int frameIndex = 0;
                Random randomizer = ((stegoKey != null) ? new Random(stegoKey) : null);

                while ((frame = grabber.grabImage()) != null) {
                    Mat mat = converter.convertToMat(frame);

                    if (mat != null && payloadIndex < totalBytes) {
                        UByteIndexer indexer = mat.createIndexer();

                        List<Integer> pixelList = new ArrayList<>(pixelsPerFrame);
                        int start = (frameIndex == 0) ? 4 : 0;

                        for (int i = start; i < pixelsPerFrame; i++) {
                            pixelList.add(i);
                        }

                        if (randomizer != null) {
                            Collections.shuffle(pixelList, randomizer);
                        }

                        if (!headerDone) {
                            for (int i = 0; i < 4; i++) {
                                byte sizeByte = (byte) ((payloadLen >> (24 - i * 8)) & 255);
                                embedByte(indexer, width, i, sizeByte);
                            }
                            headerDone = true;
                        }

                        int pixelIndex = 0;
                        while (payloadIndex < payloadLen && pixelIndex < pixelList.size()) {
                            int pos = pixelList.get(pixelIndex);
                            embedByte(indexer, width, pos, payload[payloadIndex]);

                            payloadIndex++;
                            pixelIndex++;
                        }

                        Frame modified = converter.convert(mat);
                        recorder.record(modified);
                        mat.release();

                    } else {
                        recorder.record(frame);
                    }
                    frameIndex++;
                }

                if (payloadIndex < payloadLen) {
                    throw new RuntimeException("Ga muat bro");
                }
            }
        }
    }
}