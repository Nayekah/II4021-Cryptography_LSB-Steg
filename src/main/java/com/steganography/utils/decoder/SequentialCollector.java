package com.steganography.utils.decoder;

import com.steganography.video.Extractor;

public final class SequentialCollector {
    private final long capacityBytes;
    private final byte[] headerBytes = new byte[Extractor.HEADER_SIZE];
    private HeaderInfo header;
    private byte[] payloadBytes;
    private int writeIndex;

    public SequentialCollector(long capacityBytes) {
        this.capacityBytes = capacityBytes;
    }

    public void accept(byte value) {
        if (payloadBytes == null) {
            headerBytes[writeIndex++] = value;
            if (writeIndex == headerBytes.length) {
                header = Extractor.parseHeader(headerBytes);
                if (!Extractor.isHeaderSane(header, capacityBytes)) {
                    throw new IllegalArgumentException("Could not parse valid stego header.");
                }

                payloadBytes = new byte[Extractor.HEADER_SIZE + header.fileNameLen + header.dataSize];
                System.arraycopy(headerBytes, 0, payloadBytes, 0, headerBytes.length);
            }
            return;
        }
        payloadBytes[writeIndex++] = value;
    }

    public boolean isComplete() {return payloadBytes != null && writeIndex >= payloadBytes.length;}
    public HeaderInfo getHeader() {return header;}
    public byte[] getPayloadBytes() {return payloadBytes;}
}