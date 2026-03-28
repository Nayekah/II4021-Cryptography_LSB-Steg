package com.steganography.video.mp4;

public final class DecodedMp4Payload {
    public final Mp4HeaderInfo header;
    public final String fileName;
    public final byte[] data;

    DecodedMp4Payload(Mp4HeaderInfo header, String fileName, byte[] data) {
        this.header = header;
        this.fileName = fileName;
        this.data = data;
    }
}