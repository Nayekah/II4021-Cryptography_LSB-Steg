package com.steganography.utils.encoder;

public final class PixelUpdate {
    private final int newRgb;
    private final int nextBitIndex;
    private final long squaredError;

    public PixelUpdate(int newRgb, int nextBitIndex, long squaredError) {
        this.newRgb = newRgb;
        this.nextBitIndex = nextBitIndex;
        this.squaredError = squaredError;
    }

    public int newRgb() {return newRgb;}
    public int nextBitIndex() {return nextBitIndex;}
    public long squaredError() {return squaredError;}
}