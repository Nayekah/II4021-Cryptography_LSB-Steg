package com.steganography.video.mp4;

public final class BoxLocation {
    public final int boxOffset;
    public final int boxEnd;
    public final int payloadOffset;
    public final int payloadLength;

    BoxLocation(int boxOffset, int boxEnd, int payloadOffset, int payloadLength) {
        this.boxOffset = boxOffset;
        this.boxEnd = boxEnd;
        this.payloadOffset = payloadOffset;
        this.payloadLength = payloadLength;
    }
}