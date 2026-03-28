package com.steganography.video;

import java.io.File;

public enum VideoContainer {
    AVI("avi"),
    MP4("mp4");

    private final String extension;

    VideoContainer(String extension) {this.extension = extension;}
    public String getExtension() {return extension;}

    public static VideoContainer fromFile(File file) {return fromName(file.getName());}

    public static VideoContainer fromName(String name) {
        String lower = name.toLowerCase();
        if (lower.endsWith(".mp4")) {
            return MP4;
        }
        return AVI;
    }

    @Override
    public String toString() {return extension.toUpperCase();}
}