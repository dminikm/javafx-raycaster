package app;

import java.io.File;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

class Texture {
    private Texture() {
        
    }
    
    public Color getColor(int x, int y) {
        return this.data[this.height * y + x];
    }

    static Texture from_image_path(String path) throws Exception {
        File f = new File(path);
        
        if (!f.exists()) {
            throw new RuntimeException("Could not find file: " + path);
        }

        String pth = f.toURI().toString();
        return Texture.from_image(new Image(pth));
    }

    static Texture from_image(Image img) {
        Texture t = new Texture();
        PixelReader r = img.getPixelReader();

        t.data = new Color[(int)(img.getWidth() * img.getHeight())];
        t.width = (int)img.getWidth();
        t.height = (int)img.getHeight();

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                t.data[(int)(y * img.getWidth() + x)] = r.getColor(x, y);
            }
        }

        return t;
    }


    protected Color data[];
    protected int width;
    protected int height;
}