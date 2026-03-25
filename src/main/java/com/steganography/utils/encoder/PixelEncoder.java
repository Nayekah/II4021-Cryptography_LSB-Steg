package com.steganography.utils.encoder;

import com.steganography.utils.common.Scheme;

public final class PixelEncoder {
    private final Scheme scheme;

    public PixelEncoder(Scheme scheme) {
        this.scheme = scheme;
    }

    public PixelUpdate embedIntoPixel(int rgb, byte[] payload, int bitIndex, int totalBits) {
        int rOld = (rgb >> 16) & 0xFF;
        int gOld = (rgb >> 8) & 0xFF;
        int bOld = rgb & 0xFF;

        int rNew = rOld;
        int gNew = gOld;
        int bNew = bOld;
        long sse = 0L;

        int redBits = scheme.getRedBits();
        int greenBits = scheme.getGreenBits();
        int blueBits = scheme.getBlueBits();
        int bitsUsed, bits, delta;

        if (bitIndex < totalBits) {
            bitsUsed = Math.min(redBits, totalBits - bitIndex);
            bits = extractBitsFromPayload(payload, bitIndex, bitsUsed);
            rNew = replaceLowerBits(rOld, bits, bitsUsed);
            delta = rNew - rOld;
            sse += (long) delta * delta;
            bitIndex += bitsUsed;
        }

        if (bitIndex < totalBits) {
            bitsUsed = Math.min(greenBits, totalBits - bitIndex);
            bits = extractBitsFromPayload(payload, bitIndex, bitsUsed);
            gNew = replaceLowerBits(gOld, bits, bitsUsed);
            delta = gNew - gOld;
            sse += (long) delta * delta;
            bitIndex += bitsUsed;
        }

        if (bitIndex < totalBits) {
            bitsUsed = Math.min(blueBits, totalBits - bitIndex);
            bits = extractBitsFromPayload(payload, bitIndex, bitsUsed);
            bNew = replaceLowerBits(bOld, bits, bitsUsed);
            delta = bNew - bOld;
            sse += (long) delta * delta;
            bitIndex += bitsUsed;
        }

        int newRgb = (0xFF << 24) | (rNew << 16) | (gNew << 8) | bNew;
        return new PixelUpdate(newRgb, bitIndex, sse);
    }

    public PixelUpdate embedByteIntoPixel(int rgb, byte payloadByte) {
        return embedIntoPixel(rgb, new byte[] { payloadByte }, 0, 8);
    }

    private static int replaceLowerBits(int original, int bits, int bitCount) {
        if (bitCount <= 0) {
            return original;
        }
        int mask = (1 << bitCount) - 1;
        return (original & ~mask) | (bits & mask);
    }

    private static int extractBitsFromPayload(byte[] payload, int bitOffset, int count) {
        int value = 0;
        int byteIdx, bitIdx, bit;

        for (int i = 0; i < count; i++) {
            byteIdx = (bitOffset + i) / 8;
            bitIdx = 7 - ((bitOffset + i) % 8);
            bit = (payload[byteIdx] >> bitIdx) & 1;
            value = (value << 1) | bit;
        }
        return value;
    }
}