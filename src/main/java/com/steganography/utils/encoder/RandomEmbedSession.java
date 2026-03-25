package com.steganography.utils.encoder;

import java.awt.image.BufferedImage;
import com.steganography.video.Embedder;

public final class RandomEmbedSession {
    private final byte[] payload;
    private final long baseSeed;
    private final PixelEncoder PixelEncoder;
    private int nextPayloadIndex;
    private int frameIndex;
    private long squaredError;

    public RandomEmbedSession(byte[] payload, long baseSeed, PixelEncoder PixelEncoder) {
        this.payload = payload;
        this.baseSeed = baseSeed;
        this.PixelEncoder = PixelEncoder;
    }

    public void embedFrame(BufferedImage frame) {
        if (isComplete()) {
            return;
        }

        int width = frame.getWidth();
        int height = frame.getHeight();
        int framePixels = width * height;
        int bytesThisFrame = Math.min(payload.length - nextPayloadIndex, framePixels);
        long[] positions = Embedder.sampleRandomPositions(framePixels, bytesThisFrame, Embedder.computeFrameSeed(baseSeed, frameIndex));

        for (int i = 0; i < bytesThisFrame; i++) {
            int pixelIndex = (int) positions[i];
            int x = pixelIndex % width;
            int y = pixelIndex / width;
            PixelUpdate mutation = PixelEncoder.embedByteIntoPixel(frame.getRGB(x, y), payload[nextPayloadIndex + i]);
            frame.setRGB(x, y, mutation.newRgb());
            squaredError += mutation.squaredError();
        }
        nextPayloadIndex += bytesThisFrame;
        frameIndex++;
    }

    public boolean isComplete() {return nextPayloadIndex >= payload.length;}
    public long getSquaredError() {return squaredError;}
    public long getRequiredBits() {return (long) payload.length * 8L;}
    public int getRequiredPixels() {return payload.length;}
}