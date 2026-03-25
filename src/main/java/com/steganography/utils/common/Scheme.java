package com.steganography.utils.common;

public enum Scheme {
    RGB_332(1, "3-3-2", 3, 3, 2),
    RGB_323(2, "3-2-3", 3, 2, 3),
    RGB_233(3, "2-3-3", 2, 3, 3);

    private final int id;
    private final String label;
    private final int redBits;
    private final int greenBits;
    private final int blueBits;

    Scheme(int id, String label, int redBits, int greenBits, int blueBits) {
        this.id = id;
        this.label = label;
        this.redBits = redBits;
        this.greenBits = greenBits;
        this.blueBits = blueBits;
    }

    public int getId() {return id;}
    public int getRedBits() {return redBits;}
    public int getGreenBits() {return greenBits;}
    public int getBlueBits() {return blueBits;}
    public int getBitsPerPixel() {return redBits + greenBits + blueBits;}

    public static Scheme fromId(int id) {
        for (Scheme scheme : values()) {
            if (scheme.id == id) {
                return scheme;
            }
        }
        return null;
    }

    @Override
    public String toString() {return label;}
}