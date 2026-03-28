package com.steganography.video.mp4;

public final class RawMp4Box {
    public final boolean isFile;
    public final boolean wasEncrypted;
    public final boolean wasRandom;
    public final int logicalSize;
    public final byte[] carrierBytes;
    public final BoxLocation location;

    RawMp4Box(boolean isFile, boolean wasEncrypted, boolean wasRandom, int logicalSize, byte[] carrierBytes, BoxLocation location) {
        this.isFile = isFile;
        this.wasEncrypted = wasEncrypted;
        this.wasRandom = wasRandom;
        this.logicalSize = logicalSize;
        this.carrierBytes = carrierBytes;
        this.location = location;
    }
}