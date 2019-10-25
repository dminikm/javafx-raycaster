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

        this.buffer = new Backbuffer(this.canvasWidth, this.canvasHeight);

        try {
            this.textures = new Texture[] {
                Texture.from_image_path("data/eagle.png"),
                Texture.from_image_path("data/redbrick.png"),
                Texture.from_image_path("data/purplestone.png"),
                Texture.from_image_path("data/greystone.png"),
                Texture.from_image_path("data/bluestone.png"),
                Texture.from_image_path("data/mossy.png"),
                Texture.from_image_path("data/wood.png"),
                Texture.from_image_path("data/colorstone.png")
            };
        } catch (Exception e) {};


        this.pos = new Vec2(22, 12);
        this.dir = new Vec2(-1, 0);
        this.plane = new Vec2(0, 0.66);

        this.world = World.fromFile("");

    }

    @Override
    public void handle(long currentNanoTime) {
        double delta = (currentNanoTime - lastNanoTime) / 1000000000.0;
        this.lastNanoTime = currentNanoTime;

        this.udpate(delta);
        this.render(delta);

        pw.setPixels(0, 0, this.canvasWidth, this.canvasHeight, buffer.getPixelFormat(), buffer.getData(), 0, buffer.getWidth());
        gc.setFill( Color.WHITE );
        gc.setLineWidth(2);
        Font theFont = Font.font( "Consolas", FontWeight.NORMAL, 12);
        gc.setFont( theFont );
        gc.fillText( "FPS: " + (1 / delta), 10, 10 );
    }

    private void render(double delta) {
        buffer.clear();

        for (int x = 0; x < this.canvasWidth; x++) {
            double cameraX = 2 * x / (double)this.canvasWidth - 1;
            Vec2 rayDir = dir.add(plane.mul(cameraX));

            RayResult res = this.world.castRay(RaycastMode.RaycastWorld, pos, rayDir);

            int lineHeigth = (int)(this.canvasHeight / res.distance);
            int drawStart = Math.max(0, -lineHeigth / 2 + this.canvasHeight / 2);
            int drawEnd = Math.min(this.canvasHeight - 1, lineHeigth / 2 + this.canvasHeight / 2);

            int texNum = this.world.getBlockFromRayResult(res) - 1;
            double wallX;
            if (res.side == 0) {
                wallX = pos.y + res.distance * rayDir.y;
            } else {
                wallX = pos.x + res.distance * rayDir.x;
            }

            wallX -= Math.floor(wallX);

            Texture t = textures[texNum];
            int texX = (int)(wallX * (double)(t.width));
            if ((res.side == 0 && rayDir.x > 0) ||
                (res.side == 1 && rayDir.y < 0)) {
                texX = t.width - texX - 1;
            }

            for (int y = drawStart; y < drawEnd; y++) {
                int d = (int)(y * 256 - this.canvasHeight * 128 + lineHeigth * 128);
                int texY = ((d * t.height) / lineHeigth) / 256;
                int c = t.getColor(texX, texY);
                
                if(res.side == 1) c = 0xFF000000 | (((c & 0xFFFFFF) >> 1) & 8355711);

                buffer.setPixel(x, y, c);
            }
        }
    }

    private void udpate(double delta) {
        dir = Vec2.fromAngle(dir.toAngle() + 60 * delta);
        plane = Vec2.fromAngle(plane.toAngle() + 60 * delta).mul(plane.len());
    }

    private long lastNanoTime;
    
    private Texture[] textures;
    private Backbuffer buffer;
    private GraphicsContext gc;
    private PixelWriter pw;


    private int canvasWidth;
    private int canvasHeight;

    private Vec2 pos;
    private Vec2 dir;
    private Vec2 plane;

    World world;
}