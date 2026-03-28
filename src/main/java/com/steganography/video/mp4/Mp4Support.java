package com.steganography.video.mp4;

import com.steganography.crypto.A5;
import com.steganography.crypto.SHA256;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

public final class Mp4Support {
    public static final int FLAG_FILE = 0x01;
    public static final int FLAG_ENCRYPTED = 0x02;
    public static final int FLAG_RANDOM = 0x04;
    public static final int BOX_HEADER_SIZE = 24;
    public static final int RAW_HEADER_SIZE = 13;
    public static final int LOGICAL_HEADER_SIZE = 38;
    public static final int HASH_SIZE = 32;
    public static final long MAX_BOX_SIZE = 0xFFFFFFFFL;

    private static final byte[] BOX_TYPE = new byte[]{'u', 'u', 'i', 'd'};
    private static final byte[] BOX_UUID = new byte[]{0x53, 0x54, 0x45, 0x47, 0x4d, 0x50, 0x34, 0x50,
            0x41, 0x59, 0x4c, 0x4f, 0x41, 0x44, 0x30, 0x31
    };
    private static final byte[] MAGIC = new byte[]{'S', 'T', 'E', 'G', 'M', 'P', '4', 0x01};
    private static final byte[] MDAT_TYPE = new byte[]{'m', 'd', 'a', 't'};

    private Mp4Support() {}

    public static long calculateCapacityBytes(long coverBytes) {
        long boxBudget = MAX_BOX_SIZE - coverBytes - BOX_HEADER_SIZE - RAW_HEADER_SIZE;
        long logicalBudget = Math.max(0L, boxBudget / 8L);
        return Math.max(0L, logicalBudget - LOGICAL_HEADER_SIZE);
    }

    public static void validateCapacityBytes(long requiredBytes, long capacityBytes) {
        if (requiredBytes > capacityBytes) {
            throw new IllegalArgumentException(String.format(
                    "Message too large! Need %d bytes but only %d bytes available (%.2f KB > %.2f KB)",
                    requiredBytes, capacityBytes, requiredBytes / 1024.0, capacityBytes / 1024.0));
        }
    }

    public static Mp4Payload buildPayload(byte[] payload, boolean isFile, String fileName, boolean encrypt, String encKey, boolean randomMode, String stegoKey) {
        byte[] fileNameBytes = isFile && fileName != null ? fileName.getBytes(StandardCharsets.UTF_8) : new byte[0];
        byte[] payloadHash = SHA256.digest(payload);
        byte[] storedData = payload;
        byte flags = 0;
        byte[] logicalBytes;
        byte[] carrierBytes;
        ByteBuffer logicalBuffer;
        ByteBuffer rawBuffer;

        if (isFile) {
            flags |= FLAG_FILE;
        }
        if (encrypt) {
            flags |= FLAG_ENCRYPTED;
        }
        if (randomMode) {
            flags |= FLAG_RANDOM;
        }

        if (encrypt && encKey != null && !encKey.isEmpty()) {
            storedData = new A5().encrypt(storedData, deriveKey(encKey), 0);
        }

        logicalBuffer = ByteBuffer.allocate(LOGICAL_HEADER_SIZE + fileNameBytes.length + storedData.length);
        logicalBuffer.putInt(payload.length);
        logicalBuffer.putShort((short) fileNameBytes.length);
        logicalBuffer.put(payloadHash);
        logicalBuffer.put(fileNameBytes);
        logicalBuffer.put(storedData);
        logicalBytes = logicalBuffer.array();
        carrierBytes = encodeLsbBytes(logicalBytes, randomMode, stegoKey);

        rawBuffer = ByteBuffer.allocate(RAW_HEADER_SIZE + carrierBytes.length);
        rawBuffer.put(MAGIC);
        rawBuffer.put(flags);
        rawBuffer.putInt(logicalBytes.length);
        rawBuffer.put(carrierBytes);
        return new Mp4Payload(rawBuffer.array());
    }

    public static RawMp4Box parseHiddenBox(byte[] fileBytes) {
        BoxLocation location = findHiddenBox(fileBytes);
        ByteBuffer buffer;
        byte[] magic = new byte[MAGIC.length];
        byte flags;
        int logicalSize;
        byte[] carrierBytes;

        if (location == null || location.payloadLength < RAW_HEADER_SIZE) {
            return null;
        }

        buffer = ByteBuffer.wrap(fileBytes, location.payloadOffset, location.payloadLength);
        buffer.get(magic);
        if (!Arrays.equals(magic, MAGIC)) {
            return null;
        }

        flags = buffer.get();
        logicalSize = buffer.getInt();
        if (logicalSize < LOGICAL_HEADER_SIZE || buffer.remaining() < logicalSize * 8) {
            return null;
        }

        carrierBytes = new byte[buffer.remaining()];
        buffer.get(carrierBytes);
        return new RawMp4Box((flags & FLAG_FILE) != 0, (flags & FLAG_ENCRYPTED) != 0,
                (flags & FLAG_RANDOM) != 0, logicalSize, carrierBytes, location);
    }

    public static byte[] attachHiddenBox(byte[] fileBytes, byte[] boxPayload) {
        byte[] baseBytes = stripTrailingHiddenBox(fileBytes);
        byte[] normalized = normalizeTrailingMdat(baseBytes);
        byte[] box = createUuidBox(boxPayload);
        byte[] result = Arrays.copyOf(normalized, normalized.length + box.length);

        System.arraycopy(box, 0, result, normalized.length, box.length);
        return result;
    }

    public static DecodedMp4Payload decodePayload(RawMp4Box box, String stegoKey, String encKey) {
        byte[] logicalBytes = decodeLsbBytes(box.carrierBytes, box.logicalSize, box.wasRandom, stegoKey);
        ByteBuffer buffer = ByteBuffer.wrap(logicalBytes);
        int dataSize = buffer.getInt();
        int fileNameLen = Short.toUnsignedInt(buffer.getShort());
        byte[] payloadHash = new byte[HASH_SIZE];
        byte[] fileNameBytes;
        byte[] storedData;
        byte[] finalData;

        buffer.get(payloadHash);
        if (dataSize < 0 || fileNameLen < 0 || buffer.remaining() < fileNameLen) {
            throw new IllegalArgumentException("Invalid MP4 payload metadata.");
        }

        fileNameBytes = new byte[fileNameLen];
        buffer.get(fileNameBytes);
        storedData = new byte[buffer.remaining()];
        buffer.get(storedData);

        finalData = box.wasEncrypted ? decrypt(storedData, encKey) : storedData;
        if (finalData.length != dataSize) {
            throw new IllegalArgumentException("Invalid MP4 payload length.");
        }
        if (!Arrays.equals(SHA256.digest(finalData), payloadHash)) {
            throw new IllegalArgumentException("MP4 payload integrity check failed. Wrong stego-key/A5 key or corrupted file.");
        }

        return new DecodedMp4Payload(new Mp4HeaderInfo(box.isFile, box.wasEncrypted, box.wasRandom, dataSize, fileNameLen, payloadHash), new String(fileNameBytes, StandardCharsets.UTF_8), finalData);
    }

    public static long computeSeed(String stegoKey) {
        long seed = stegoKey.hashCode();
        for (int i = 0; i < stegoKey.length(); i++) {
            seed = seed * 31L + stegoKey.charAt(i);
        }
        return seed;
    }

    private static byte[] encodeLsbBytes(byte[] logicalBytes, boolean randomMode, String stegoKey) {
        byte[] carrier = new byte[logicalBytes.length * 8];
        int[] positions = buildPositions(carrier.length, randomMode, stegoKey);
        Random filler = new Random(0x5A17C3E5D9L ^ logicalBytes.length);
        int bit;
        int carrierIndex;

        filler.nextBytes(carrier);
        for (int bitOffset = 0; bitOffset < logicalBytes.length * 8; bitOffset++) {
            bit = extractBit(logicalBytes, bitOffset);
            carrierIndex = positions[bitOffset];
            carrier[carrierIndex] = (byte) ((carrier[carrierIndex] & 0xFE) | bit);
        }
        return carrier;
    }

    private static byte[] decodeLsbBytes(byte[] carrierBytes, int logicalSize, boolean randomMode, String stegoKey) {
        byte[] logical = new byte[logicalSize];
        int[] positions = buildPositions(logicalSize * 8, randomMode, stegoKey);
        int carrierIndex;
        int bit;

        if (carrierBytes.length < logicalSize * 8) {
            throw new IllegalArgumentException("Invalid MP4 carrier length.");
        }

        for (int bitOffset = 0; bitOffset < logicalSize * 8; bitOffset++) {
            carrierIndex = positions[bitOffset];
            bit = carrierBytes[carrierIndex] & 1;
            writeBit(logical, bitOffset, bit);
        }
        return logical;
    }

    private static int[] buildPositions(int count, boolean randomMode, String stegoKey) {
        int[] positions = new int[count];
        Random random;
        int j;
        int tmp;

        if (randomMode && (stegoKey == null || stegoKey.isBlank())) {
            throw new IllegalArgumentException("This MP4 payload was embedded in random mode. Please provide the stego-key.");
        }

        for (int i = 0; i < count; i++) {
            positions[i] = i;
        }
        if (!randomMode) {
            return positions;
        }

        random = new Random(computeSeed(stegoKey));
        for (int i = count - 1; i > 0; i--) {
            j = random.nextInt(i + 1);
            tmp = positions[i];
            positions[i] = positions[j];
            positions[j] = tmp;
        }
        return positions;
    }

    private static byte[] decrypt(byte[] storedData, String encKey) {
        if (encKey == null || encKey.isBlank()) {
            throw new IllegalArgumentException("The payload is encrypted, please provide the A5/1 key to extract it.");
        }
        return new A5().decrypt(storedData, deriveKey(encKey), 0);
    }

    private static int extractBit(byte[] source, int bitOffset) {
        int byteIndex = bitOffset / 8;
        int bitIndex = 7 - (bitOffset % 8);
        return (source[byteIndex] >> bitIndex) & 1;
    }

    private static void writeBit(byte[] target, int bitOffset, int bit) {
        int byteIndex = bitOffset / 8;
        int bitIndex = 7 - (bitOffset % 8);
        target[byteIndex] = (byte) (target[byteIndex] | (bit << bitIndex));
    }

    private static byte[] createUuidBox(byte[] payload) {
        long size = BOX_HEADER_SIZE + (long) payload.length;
        ByteBuffer buffer;

        if (size > MAX_BOX_SIZE) {
            throw new IllegalArgumentException("MP4 hidden box exceeds 32-bit MP4 box limit.");
        }

        buffer = ByteBuffer.allocate((int) size).order(ByteOrder.BIG_ENDIAN);
        buffer.putInt((int) size);
        buffer.put(BOX_TYPE);
        buffer.put(BOX_UUID);
        buffer.put(payload);
        return buffer.array();
    }

    private static byte[] stripTrailingHiddenBox(byte[] fileBytes) {
        BoxLocation location = findHiddenBox(fileBytes);
        if (location != null && location.boxEnd == fileBytes.length) {
            return Arrays.copyOf(fileBytes, location.boxOffset);
        }
        return fileBytes;
    }

    private static BoxLocation findHiddenBox(byte[] fileBytes) {
        int offset = 0;
        BoxLocation found = null;
        long size;
        int headerSize;
        int payloadOffset;
        int payloadLength;

        while (offset + 8 <= fileBytes.length) {
            size = readUInt32(fileBytes, offset);
            headerSize = 8;

            if (size == 0) {
                break;
            }
            if (size == 1) {
                if (offset + 16 > fileBytes.length) {
                    break;
                }
                size = readUInt64(fileBytes, offset + 8);
                headerSize = 16;
            }
            if (size < headerSize || offset + size > fileBytes.length) {
                break;
            }

            if (matches(fileBytes, offset + 4, BOX_TYPE)) {
                payloadOffset = offset + headerSize;
                payloadLength = (int) size - headerSize;
                if (payloadLength >= BOX_UUID.length + MAGIC.length && matches(fileBytes, payloadOffset, BOX_UUID)) {
                    found = new BoxLocation(offset, offset + (int) size,
                            payloadOffset + BOX_UUID.length, payloadLength - BOX_UUID.length);
                }
            }
            offset += (int) size;
        }
        return found;
    }

    private static byte[] normalizeTrailingMdat(byte[] fileBytes) {
        int offset = 0;
        long size;

        while (offset + 8 <= fileBytes.length) {
            size = readUInt32(fileBytes, offset);
            if (size == 0) {
                if (matches(fileBytes, offset + 4, MDAT_TYPE)) {
                    byte[] normalized = Arrays.copyOf(fileBytes, fileBytes.length);
                    ByteBuffer.wrap(normalized, offset, 4).order(ByteOrder.BIG_ENDIAN).putInt(fileBytes.length - offset);
                    return normalized;
                }
                break;
            }
            if (size == 1 || size < 8 || offset + size > fileBytes.length) {
                break;
            }
            offset += (int) size;
        }
        return fileBytes;
    }

    private static boolean matches(byte[] buffer, int offset, byte[] expected) {
        if (offset < 0 || offset + expected.length > buffer.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if (buffer[offset + i] != expected[i]) {
                return false;
            }
        }
        return true;
    }

    private static long readUInt32(byte[] buffer, int offset) {return ByteBuffer.wrap(buffer, offset, 4).order(ByteOrder.BIG_ENDIAN).getInt() & 0xFFFFFFFFL;}
    private static long readUInt64(byte[] buffer, int offset) {return ByteBuffer.wrap(buffer, offset, 8).order(ByteOrder.BIG_ENDIAN).getLong();}

    private static long deriveKey(String userKey) {
        byte[] hash = SHA256.digest(userKey.getBytes(StandardCharsets.UTF_8));
        return ByteBuffer.wrap(hash, 0, Long.BYTES).getLong();
    }
}