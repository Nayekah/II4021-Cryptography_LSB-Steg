package com.steganography.utils.common;

import java.awt.image.BufferedImage;
import java.util.List;

public class Metrics {
    // MSE = (1/MN) * sum((C(i,j)-S(i,j))^2)
    public static double computeMSE(BufferedImage original, BufferedImage stego) {
        int w = original.getWidth();
        int h = original.getHeight();
        long sumSquared = 0;
        int totalSamples = 0;

        int rgbOrig, rgbSteg, rO, gO, bO, rS, gS, bS;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                rgbOrig = original.getRGB(x, y);
                rgbSteg = stego.getRGB(x, y);

                rO = (rgbOrig >> 16) & 0xFF;
                gO = (rgbOrig >> 8) & 0xFF;
                bO = rgbOrig & 0xFF;

                rS = (rgbSteg >> 16) & 0xFF;
                gS = (rgbSteg >> 8) & 0xFF;
                bS = rgbSteg & 0xFF;

                sumSquared += (long) (rO - rS) * (rO - rS);
                sumSquared += (long) (gO - gS) * (gO - gS);
                sumSquared += (long) (bO - bS) * (bO - bS);
                totalSamples += 3;
            }
        }
        return (double) sumSquared / totalSamples;
    }

    // PSNR = 10 * log10(MAX_I^2 / MSE)
    public static double computePSNR(double mse) {
        if (mse == 0) return Double.POSITIVE_INFINITY;
        return 10.0 * Math.log10((255.0 * 255.0) / mse);
    }

    public static double computeAverageMSE(List<BufferedImage> originals, List<BufferedImage> stegos) {
        if (originals.size() != stegos.size()) {
            throw new IllegalArgumentException("Frame lists must be the same size.");
        }
        double totalMSE = 0;

        for (int i = 0; i < originals.size(); i++) {
            totalMSE += computeMSE(originals.get(i), stegos.get(i));
        }
        return totalMSE / originals.size();
    }

    public static double computeAveragePSNR(List<BufferedImage> originals, List<BufferedImage> stegos) {
        double avgMSE = computeAverageMSE(originals, stegos);
        return computePSNR(avgMSE);
    }

    public static int[][] generateHistogramData(BufferedImage frame) {
        int[][] histogram = new int[3][256];
        int w = frame.getWidth();
        int h = frame.getHeight();
        
        int rgb, r, g, b;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                rgb = frame.getRGB(x, y);
                r = (rgb >> 16) & 0xFF;
                g = (rgb >> 8) & 0xFF;
                b = rgb & 0xFF;

                histogram[0][r]++; histogram[1][g]++; histogram[2][b]++;
            }
        }
        return histogram;
    }

    public static void accumulateHistogramData(BufferedImage frame, long[][] histogram) {
        int w = frame.getWidth();
        int h = frame.getHeight();

        int rgb, r, g, b;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                rgb = frame.getRGB(x, y);
                r = (rgb >> 16) & 0xFF;
                g = (rgb >> 8) & 0xFF;
                b = rgb & 0xFF;

                histogram[0][r]++;
                histogram[1][g]++;
                histogram[2][b]++;
            }
        }
    }

    // we'll show only the average of all frames (due to performance and memory. I js don't want to get java heap space issue again:' )
    public static int[][] averageHistogramData(long[][] totalHistogram, int frameCount) {
        int[][] averageHistogram = new int[3][256];
        if (frameCount <= 0) {
            return averageHistogram;
        }

        for (int channel = 0; channel < 3; channel++) {
            for (int intensity = 0; intensity < 256; intensity++) {
                averageHistogram[channel][intensity] = (int) Math.round((double) totalHistogram[channel][intensity] / frameCount);
            }
        }
        return averageHistogram;
    }
}