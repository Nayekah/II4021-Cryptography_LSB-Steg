package com.steganography.utils.encoder;

import java.awt.image.BufferedImage;

public final class SequentialEmbedSession {
    private final byte[] payload;
    private final PixelEncoder PixelEncoder;
    private final int totalBits;
    private int bitIndex;
    private long squaredError;

    public SequentialEmbedSession(byte[] payload, PixelEncoder PixelEncoder) {
        this.payload = payload;
        this.PixelEncoder = PixelEncoder;
        this.totalBits = payload.length * 8;
    }

    public void embedFrame(BufferedImage frame) {
        if (isComplete()) {
            return;
        }

        int width = frame.getWidth();
        int height = frame.getHeight();

        for (int y = 0; y < height && bitIndex < totalBits; y++) {
            for (int x = 0; x < width && bitIndex < totalBits; x++) {
                PixelUpdate mutation = PixelEncoder.embedIntoPixel(frame.getRGB(x, y), payload, bitIndex, totalBits);
                frame.setRGB(x, y, mutation.newRgb());
                bitIndex = mutation.nextBitIndex();
                squaredError += mutation.squaredError();
            }
        }
    }

    public boolean isComplete() {return bitIndex >= totalBits;}
    public long getSquaredError() {return squaredError;}
    public long getRequiredBits() {return totalBits;}
}