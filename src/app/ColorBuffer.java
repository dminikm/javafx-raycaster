package app;

import java.nio.IntBuffer;
import javafx.scene.image.PixelFormat;

class ARGBColor {
    public static int mixARGBIntColors(int color1, int color2) {
        int alpha = (color2 & 0xFF000000) >>> 24;
        int rb = (color1 & 0xFF00FF) + (((color2 & 0xFF00FF) - (color1 & 0xFF00FF)) * alpha >>> 8);
        int g = (color1 & 0x00FF00) + (((color2 & 0x00FF00) - (color1 & 0x00FF00)) * alpha >>> 8);
        
        return 0xFF000000 | (rb & 0xFF00FF) | (g & 0xFF00);
    }
}

public class ColorBuffer {
    public ColorBuffer(int width, int height) {
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
        return this.data[Math.abs(y * this.width + x)];
    }

    public void setPixel(int x, int y, int color) {
        this.data[Math.abs(y * this.width + x)] = color;
    }

    public void setPixelTransparent(int x, int y, int color) {
        this.setPixel(x, y, ARGBColor.mixARGBIntColors(this.getPixel(x, y), color));
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

    public void copyFromBuffer(ColorBuffer other, int x, int y) {
        for (int sy = 0; sy < other.height; sy++) {
            for (int sx = 0; sx < other.width; sx++) {
                int dx = x + sx;
                int dy = y + sy;

                this.setPixelTransparent(dx, dy, other.getPixel(sx, sy));
            }
        }
    }

    protected int[] data;
    protected int width;
    protected int height;
}
