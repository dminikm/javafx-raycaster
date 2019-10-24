package app;

import java.nio.IntBuffer;
import javafx.scene.image.PixelFormat;

public class Backbuffer {
    public Backbuffer(int width, int height) {
        this.data = new int[width * height];
        this.width = width;
        this.height = height;
    }

    public void clear() {
        for (int i = 0; i < this.data.length; i++) {
            this.data[i] = 0xFF000000;
        }
    }

    public int getPixel(int x, int y) {
        return this.data[(y * this.height) + x];
    }

    public void setPixel(int x, int y, int color) {
        this.data[y * this.width + x] = color;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int[] getData() {
        return this.data;
    }

    public PixelFormat<IntBuffer> getPixelFormat() {
        return PixelFormat.getIntArgbPreInstance();
    }

    private int[] data;
    private int width;
    private int height;
}