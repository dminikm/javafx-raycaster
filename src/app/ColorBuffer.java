package app;

import java.nio.IntBuffer;
import javafx.scene.image.PixelFormat;

class ARGBColor {
    public static int mixARGBIntColors(int color1, int color2) {
        byte a1 = (byte)((color1 & 0xFF000000) >>> 24);
        byte r1 = (byte)((color1 & 0x00FF0000) >>> 16);
        byte g1 = (byte)((color1 & 0x0000FF00) >>> 8 );
        byte b1 = (byte)((color1 & 0x000000FF) >>> 0 );
        byte a2 = (byte)((color2 & 0xFF000000) >>> 24);
        byte r2 = (byte)((color2 & 0x00FF0000) >>> 16);
        byte g2 = (byte)((color2 & 0x0000FF00) >>> 8 );
        byte b2 = (byte)((color2 & 0x000000FF) >>> 0 );

        int alpha = a1 + 1;
        int inv_alpha = 256 - alpha;

        byte r = (byte)((alpha * r2 + inv_alpha * r1) >> 8);
        byte g = (byte)((alpha * g2 + inv_alpha * g1) >> 8);
        byte b = (byte)((alpha * b2 + inv_alpha * b1) >> 8);
        
        int ra = 0xFF << 24;
        int rr = r << 16;
        int rg = g << 8;
        int rb = b;

        return ra | rr | rg | rb;
    }
}

class BGRAColor {
    public static int mixARGBIntColors(int color1, int color2) {
        double b1 = (double)((color1 & 0xFF000000) >>> 24)  / 255;
        double g1 = (double)((color1 & 0x00FF0000) >>> 16)  / 255;
        double r1 = (double)((color1 & 0x0000FF00) >>> 8 )  / 255;
        double a1 = (double)((color1 & 0x000000FF) >>> 0 )  / 255;
        double b2 = (double)((color2 & 0xFF000000) >>> 24)  / 255;
        double g2 = (double)((color2 & 0x00FF0000) >>> 16)  / 255;
        double r2 = (double)((color2 & 0x0000FF00) >>> 8 )  / 255;
        double a2 = (double)((color2 & 0x000000FF) >>> 0 )  / 255;

        double dr = r2 * a2 + r1 * a1 * (1 - a2);
        double dg = g2 * a2 + g1 * a1 * (1 - a2);
        double db = b2 * a2 + b1 * a1 * (1 - a2);
        
        int a = 0xFF << 24;
        int r = (int)(dr * 255) << 16;
        int g = (int)(dg * 255) << 8;
        int b = (int)(db * 255);

        return a | r | g | b;
    }

    public static int fromARGBColor(int color) {
        int ia = (color & 0xFF000000) >>> 24;
        int ir = (color & 0x00FF0000) >>> 16;
        int ig = (color & 0x0000FF00) >>>  8;
        int ib = (color & 0x000000FF) >>>  0;

        int b = ib <<  8;
        int g = ig << 16;
        int r = ir << 24;
        int a = ia <<  0;

        return b | g | r | a;
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

        int newColor = ARGBColor.mixARGBIntColors(this.getPixel(x, y), color);
        this.setPixel(x, y, newColor);
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

    protected int[] data;
    protected int width;
    protected int height;
}