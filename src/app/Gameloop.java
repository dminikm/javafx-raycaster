package app;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class Gameloop extends AnimationTimer {
    public Gameloop(Canvas cv) {
        this.lastNanoTime = System.nanoTime();
        this.gc = cv.getGraphicsContext2D();
        this.pw = this.gc.getPixelWriter();

        this.canvasWidth = (int)cv.getWidth();
        this.canvasHeight = (int)cv.getHeight();

        this.world = World.fromFile("");
        this.renderer = new Renderer(canvasWidth, canvasHeight, this.world);


    }

    @Override
    public void handle(long currentNanoTime) {
        double delta = (currentNanoTime - lastNanoTime) / 1000000000.0;
        this.lastNanoTime = currentNanoTime;

        this.udpate(delta);
        this.render(delta);
    }

    private void render(double delta) {
        Backbuffer buffer = this.renderer.render(delta);

        pw.setPixels(0, 0, this.canvasWidth, this.canvasHeight, buffer.getPixelFormat(), buffer.getData(), 0, buffer.getWidth());
        gc.setFill( Color.WHITE );
        gc.setLineWidth(2);
        Font theFont = Font.font( "Consolas", FontWeight.NORMAL, 12);
        gc.setFont( theFont );
        gc.fillText( "FPS: " + (1 / delta), 10, 10 );
    }

    private void udpate(double delta) {
        //dir = Vec2.fromAngle(dir.toAngle() + 60 * delta);
        //plane = Vec2.fromAngle(plane.toAngle() + 60 * delta).mul(plane.len());
    }

    private long lastNanoTime;
    
    private Renderer renderer;
    private GraphicsContext gc;
    private PixelWriter pw;


    private int canvasWidth;
    private int canvasHeight;

    World world;
}