package app;

import javafx.animation.AnimationTimer;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class Gameloop extends AnimationTimer {
    public Gameloop(Canvas cv) {
        this.sc = cv;
        this.lastNanoTime = System.nanoTime();
        this.gc = cv.getGraphicsContext2D();
        this.pw = this.gc.getPixelWriter();

        this.canvasWidth = (int)cv.getWidth();
        this.canvasHeight = (int)cv.getHeight();

        this.textureRegistry = TextureRegistry.getInstance();
        this.world = LevelLoader.loadLevel("data/levels/level01.json", textureRegistry);
        this.renderer = new Renderer(canvasWidth, canvasHeight, this.world, textureRegistry);

        this.mouseLocked = false;
    }

    private Vec2 getScreenCenter() {
        var bounds = this.sc.localToScreen(this.sc.getBoundsInLocal());
        return new Vec2(bounds.getMinX() + this.canvasWidth / 2, bounds.getMinY() + this.canvasHeight / 2);
    }

    private Vec2 getMousePosition() {
        var position = new Robot().getMousePosition();
        return new Vec2(Math.floor(position.getX()), Math.floor(position.getY()));
    }

    private Vec2 getMouseDelta() {
        var position = this.getMousePosition();
        var center = this.getScreenCenter();

        return position.sub(center);
    }

    private void lockMouse() {
        var pos = this.getScreenCenter();
        new Robot().mouseMove(pos.x, pos.y);
    }

    @Override
    public void handle(long currentNanoTime) {
        double delta = (currentNanoTime - lastNanoTime) / 1000000000.0;
        this.lastNanoTime = currentNanoTime;

        var mouseDelta = this.getMouseDelta();
        var mousePos = this.getMousePosition();

        KeyRegistry.getInstance().handleMouse(mouseDelta, mousePos);

        this.udpate(delta);
        this.render(delta);

        if (this.mouseLocked) {
            this.lockMouse();
        }

        KeyRegistry.getInstance().update(delta);
    }

    private void render(double delta) {
        ColorBuffer buffer = this.renderer.render(delta);

        pw.setPixels(0, 0, this.canvasWidth, this.canvasHeight, buffer.getPixelFormat(), buffer.getData(), 0, buffer.getWidth());
        gc.setFill( Color.WHITE );
        gc.setLineWidth(2);
        Font theFont = Font.font("Consolas", FontWeight.NORMAL, 12);
        gc.setFont( theFont );
        gc.fillText("FPS: " + (int)(1 / delta), 100, 10 );

        gc.setFill(new Color(0.1, 0.1, 0.1, 0.3));
        gc.fillRect(0, this.canvasHeight - 100, 250, 100);

        gc.setFill( Color.WHITE );
        Font healthFont = Font.font("Consolas", FontWeight.BOLD, 48);
        gc.setFont(healthFont);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText("+", 50, this.canvasHeight - 50);
        gc.fillText("" + (int)this.world.getPlayer().health, 125, this.canvasHeight - 50);
    }

    private void udpate(double delta) {
        this.world.update(delta);
    }

    private long lastNanoTime;
    
    private Canvas sc;
    private GraphicsContext gc;
    private PixelWriter pw;
    
    
    private int canvasWidth;
    private int canvasHeight;
    
    private boolean mouseLocked;
    
    private TextureRegistry textureRegistry;
    private Renderer renderer;
    private World world;
}
