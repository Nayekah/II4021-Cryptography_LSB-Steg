package com.steganography.video.mp4;

public final class Mp4HeaderInfo {
    public final boolean isFile;
    public final boolean wasEncrypted;
    public final boolean wasRandom;
    public final int dataSize;
    public final int fileNameLen;
    public final byte[] payloadHash;

    public Mp4HeaderInfo(boolean isFile, boolean wasEncrypted, boolean wasRandom, int dataSize, int fileNameLen, byte[] payloadHash) {
        this.isFile = isFile;
        this.wasEncrypted = wasEncrypted;
        this.wasRandom = wasRandom;
        this.dataSize = dataSize;
        this.fileNameLen = fileNameLen;
        this.payloadHash = payloadHash;
    }
}