package com.chavaillaz.fx.islands.showcase;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;

/**
 * Writes an opaque image to a PNG file with the classes of {@code java.base}: the tests run in the module of the
 * library, which does not read the desktop module that holds ImageIO. Each row is filtered the way its bytes compress
 * best, as the specification of PNG advises.
 */
final class Png {

    private static final byte[] SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
    private static final int BYTES_PER_PIXEL = 3;
    private static final int BIT_DEPTH = 8;
    private static final int TRUECOLOR = 2;

    private Png() {
    }

    /**
     * Writes an image, its transparency left out.
     *
     * @param image the image
     * @param file  the file written, replaced if it exists
     * @throws IOException if the file cannot be written
     */
    static void write(Image image, Path file) throws IOException {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        ByteArrayOutputStream pixels = new ByteArrayOutputStream();
        try (DeflaterOutputStream deflater = new DeflaterOutputStream(pixels,
                new Deflater(Deflater.BEST_COMPRESSION))) {
            PixelReader reader = image.getPixelReader();
            int[] argb = new int[width];
            byte[] above = new byte[width * BYTES_PER_PIXEL];
            for (int y = 0; y < height; y++) {
                reader.getPixels(0, y, width, 1, PixelFormat.getIntArgbInstance(), argb, 0, width);
                byte[] row = new byte[width * BYTES_PER_PIXEL];
                for (int x = 0; x < width; x++) {
                    row[x * BYTES_PER_PIXEL] = (byte) (argb[x] >> 16);
                    row[x * BYTES_PER_PIXEL + 1] = (byte) (argb[x] >> 8);
                    row[x * BYTES_PER_PIXEL + 2] = (byte) argb[x];
                }
                deflater.write(filter(row, above));
                above = row;
            }
        }

        ByteArrayOutputStream header = new ByteArrayOutputStream();
        try (DataOutputStream data = new DataOutputStream(header)) {
            data.writeInt(width);
            data.writeInt(height);
            data.writeByte(BIT_DEPTH);
            data.writeByte(TRUECOLOR);
            // Compression, filtering and interlacing: the only methods PNG defines, and no interlacing
            data.writeByte(0);
            data.writeByte(0);
            data.writeByte(0);
        }
        try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(file))) {
            out.write(SIGNATURE);
            chunk(out, "IHDR", header.toByteArray());
            chunk(out, "IDAT", pixels.toByteArray());
            chunk(out, "IEND", new byte[0]);
        }
    }

    /**
     * Filters a row with the filter whose bytes, as signed values, sum the lowest, the type of the filter first.
     */
    private static byte[] filter(byte[] row, byte[] above) {
        byte[] best = new byte[0];
        long lowest = Long.MAX_VALUE;
        for (int type = 0; type <= 4; type++) {
            byte[] filtered = new byte[row.length + 1];
            filtered[0] = (byte) type;
            long sum = 0;
            for (int index = 0; index < row.length; index++) {
                int left = index >= BYTES_PER_PIXEL ? row[index - BYTES_PER_PIXEL] & 0xFF : 0;
                int up = above[index] & 0xFF;
                int upLeft = index >= BYTES_PER_PIXEL ? above[index - BYTES_PER_PIXEL] & 0xFF : 0;
                int predicted = switch (type) {
                    case 1 -> left;
                    case 2 -> up;
                    case 3 -> (left + up) / 2;
                    case 4 -> paeth(left, up, upLeft);
                    default -> 0;
                };
                byte value = (byte) ((row[index] & 0xFF) - predicted);
                filtered[index + 1] = value;
                sum += Math.abs(value);
            }
            if (sum < lowest) {
                lowest = sum;
                best = filtered;
            }
        }
        return best;
    }

    /**
     * Predicts a byte from the one on its left, the one above, and the one above on the left, whichever is nearest
     * to their gradient.
     */
    private static int paeth(int left, int up, int upLeft) {
        int estimate = left + up - upLeft;
        int toLeft = Math.abs(estimate - left);
        int toUp = Math.abs(estimate - up);
        int toUpLeft = Math.abs(estimate - upLeft);
        if (toLeft <= toUp && toLeft <= toUpLeft) {
            return left;
        }
        return toUp <= toUpLeft ? up : upLeft;
    }

    private static void chunk(DataOutputStream out, String type, byte[] data) throws IOException {
        byte[] name = type.getBytes(StandardCharsets.US_ASCII);
        CRC32 checksum = new CRC32();
        checksum.update(name);
        checksum.update(data);
        out.writeInt(data.length);
        out.write(name);
        out.write(data);
        out.writeInt((int) checksum.getValue());
    }
}
