package app;

import java.io.File;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;

class Texture extends ColorBuffer {
    private Texture(int width, int height) {
        super(width, height);
    }

    static Texture loadFromImagePath(String path) throws Exception {
        File f = new File(path);
        
        if (!f.exists()) {
            throw new RuntimeException("Could not find file: " + path);
        }

        String pth = f.toURI().toString();
        return Texture.loadFromImage(new Image(pth));
    }

    static Texture loadFromImage(Image img) {
        PixelReader r = img.getPixelReader();
        
        int width = (int)img.getWidth();
        int height = (int)img.getHeight();
        
        Texture t = new Texture(width, height);
        
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                t.data[y * t.width + x] = r.getArgb(x, y);
            }
        }

        return t;
    }
}