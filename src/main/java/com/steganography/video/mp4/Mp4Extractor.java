package com.steganography.video.mp4;

import com.steganography.video.ExtractResult;

import java.io.File;
import java.nio.file.Files;

public class Mp4Extractor {
    public ExtractResult extract(File input, String stegoKey, String encKey) throws Exception {
        RawMp4Box box = Mp4Support.parseHiddenBox(Files.readAllBytes(input.toPath()));
        DecodedMp4Payload payload;

        if (box == null) {
            throw new IllegalArgumentException("Could not find embedded MP4 payload.");
        }

        payload = Mp4Support.decodePayload(box, stegoKey, encKey);
        return new ExtractResult(payload.header.isFile, payload.header.wasEncrypted, payload.header.wasRandom, payload.fileName, payload.data);
    }
}