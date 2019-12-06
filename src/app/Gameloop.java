package app;

import javafx.animation.AnimationTimer;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class Gameloop extends AnimationTimer {
    public Gameloop(Canvas cv) {
        this.cv = cv;
        this.lastNanoTime = System.nanoTime();
        this.gc = this.cv.getGraphicsContext2D();
        this.pw = this.gc.getPixelWriter();

        this.canvasWidth = (int)cv.getWidth();
        this.canvasHeight = (int)cv.getHeight();

        this.textureRegistry = TextureRegistry.getInstance();
        this.world = LevelLoader.loadLevel("data/levels/level01.json", textureRegistry);
        this.renderer = new Renderer(canvasWidth, canvasHeight, this.world, textureRegistry);
    }

    @Override
    public void handle(long currentNanoTime) {
        double delta = (currentNanoTime - lastNanoTime) / 1000000000.0;
        this.lastNanoTime = currentNanoTime;

        this.udpate(delta);
        this.render(delta);

        if (this.world.getPlayer().health <= 0) {
            this.world.resetTo(LevelLoader.loadLevel(this.world.getName(), this.textureRegistry));
        }

        KeyRegistry.getInstance().update(delta);
    }

    private void render(double delta) {
        ColorBuffer buffer = this.renderer.render(delta);

        // Copy the final bufffer back to the canvas
        pw.setPixels(0, 0, this.canvasWidth, this.canvasHeight, buffer.getPixelFormat(), buffer.getData(), 0, buffer.getWidth());

        // Draw fps
        gc.setFill( Color.WHITE );
        gc.setLineWidth(2);
        Font theFont = Font.font("Consolas", FontWeight.NORMAL, 12);
        gc.setFont( theFont );
        gc.fillText("FPS: " + (int)(1 / delta), 100, 10 );

        // Draw health/ammo box
        gc.setFill(new Color(0.1, 0.1, 0.1, 0.3));
        gc.fillRect(0, this.canvasHeight - 100, 250, 100);
        gc.fillRect(this.canvasWidth - 250, this.canvasHeight - 100, 250, 100);

        // Draw health + ammo
        gc.setFill( Color.WHITE );
        Font healthFont = Font.font("Consolas", FontWeight.BOLD, 48);
        gc.setFont(healthFont);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText("+", 50, this.canvasHeight - 50);
        gc.fillText("" + (int)this.world.getPlayer().health, 125, this.canvasHeight - 50);

        int ammo = this.world.getPlayer().getCurrentWeapon().getAmmo();
        gc.fillText("" + ((ammo < 0) ? "Inf" : ammo), this.canvasWidth - 125, this.canvasHeight - 50);

        // Draw crosshair
        int centerX = this.canvasWidth / 2;
        int centerY = this.canvasHeight / 2;

        gc.setFill(Color.BLACK);
        gc.fillRect(centerX - 15, centerY - 2, 12, 4);
        gc.fillRect(centerX + 3, centerY - 2, 12, 4);
        gc.fillRect(centerX - 2, centerY - 15, 4, 12);
        gc.fillRect(centerX - 2, centerY + 3, 4, 12);

        gc.setFill(Color.WHITE);
        gc.fillRect(centerX - 14, centerY - 1, 10, 2);
        gc.fillRect(centerX + 4, centerY - 1, 10, 2);
        gc.fillRect(centerX - 1, centerY - 14, 2, 10);
        gc.fillRect(centerX - 1, centerY + 4, 2, 10);
    }

    private void udpate(double delta) {
        this.world.update(delta);
    }

    private long lastNanoTime;
    
    private Canvas cv;
    private GraphicsContext gc;
    private PixelWriter pw;
    
    
    private int canvasWidth;
    private int canvasHeight;
    
    private TextureRegistry textureRegistry;
    private Renderer renderer;
    private World world;
}
