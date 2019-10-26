package app;

import java.nio.IntBuffer;
import javafx.scene.image.PixelFormat;

class ARGBColor {
    public ARGBColor(double r, double g, double b, double a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public int toARGBInt() {
        int a = (int)(this.a * 255) << 24;
        int r = (int)(this.r * 255) << 16;
        int g = (int)(this.g * 255) << 8;
        int b = (int)(this.b * 255);

        return a | r | g | b;
    }

    public static ARGBColor fromARGBInt(int color) {
        double a = (double)((color & 0xFF000000) >>> 24)  / 255;
        double r = (double)((color & 0x00FF0000) >>> 16)  / 255;
        double g = (double)((color & 0x0000FF00) >>> 8 )  / 255;
        double b = (double)((color & 0x000000FF) >>> 0 )  / 255;

        return new ARGBColor(r, g, b, a);
    }

    public ARGBColor mix(ARGBColor other) {
        double a = 1;

        double r = other.r * other.a + this.r * this.a * (1 - other.a);
        double g = other.g * other.a + this.g * this.a * (1 - other.a);
        double b = other.b * other.a + this.b * this.a * (1 - other.a);

        return new ARGBColor(r, g, b, a);
    }

    public double r;
    public double g;
    public double b;
    public double a;
}

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

    public void clearRect(int x, int y, int w, int h) {
        for (int i = y; i < y + h; i++) {
            for (int o = x; o < x + w; o++) {
                this.data[i * this.width + o] = 0xFF000000;
            }
        }
    }

    public int getPixel(int x, int y) {
        return this.data[y * this.width + x];
    }

    public void setPixel(int x, int y, int color) {
        this.data[y * this.width + x] = color;
    }

    public void setPixelTransparent(int x, int y, int color) {
        // If pixel is fully opaque, just write it
        int alpha = (color & 0xFF000000) >>> 24;
        if (alpha == 255) {
            this.setPixel(x, y, color); return;
        }

        var oldColor = ARGBColor.fromARGBInt(this.getPixel(x, y));
        oldColor.a = 1;

        var newColor = ARGBColor.fromARGBInt(color);

        this.setPixel(x, y, oldColor.mix(newColor).toARGBInt());
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