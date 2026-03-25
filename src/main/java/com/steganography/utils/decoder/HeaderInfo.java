package com.steganography.utils.decoder;

import com.steganography.utils.common.Scheme;

public final class HeaderInfo {
    public final boolean isFile;
    public final boolean wasEncrypted;
    public final boolean wasRandom;
    public final Scheme scheme;
    public final int dataSize;
    public final int fileNameLen;
    public final int flags;

    public HeaderInfo(boolean isFile, boolean wasEncrypted, boolean wasRandom, Scheme scheme, int dataSize, int fileNameLen, int flags) {
        this.isFile = isFile;
        this.wasEncrypted = wasEncrypted;
        this.wasRandom = wasRandom;
        this.scheme = scheme;
        this.dataSize = dataSize;
        this.fileNameLen = fileNameLen;
        this.flags = flags;
    }
}