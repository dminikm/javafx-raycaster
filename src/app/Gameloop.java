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

            int mapX = (int)pos.x;
            int mapY = (int)pos.y;

            Vec2 sideDist = new Vec2();

            Vec2 deltaDist = new Vec2(Math.abs(1 / rayDir.x), Math.abs(1 / rayDir.y));
            double perpWallDist;

            int stepX;
            int stepY;

            int hit = 0;
            int side = 0;

            if (rayDir.x < 0) {
                stepX = -1;
                sideDist.x = (pos.x - mapX) * deltaDist.x;
            } else {
                stepX = 1;
                sideDist.x = (mapX + 1.0 - pos.x) * deltaDist.x;
            }

            if (rayDir.y < 0) {
                stepY = -1;
                sideDist.y = (pos.y - mapY) * deltaDist.y;
            } else {
                stepY = 1;
                sideDist.y = (mapY + 1.0 - pos.y) * deltaDist.y;
            }

            while (hit == 0) {
                if (sideDist.x < sideDist.y) {
                    sideDist.x += deltaDist.x;
                    mapX += stepX;
                    side = 0;
                } else {
                    sideDist.y += deltaDist.y;
                    mapY += stepY;
                    side = 1;
                }

                if (worldMap[mapY][mapX] > 0) {
                    hit = 1;
                }
            }

            if (side == 0) {
                perpWallDist = (mapX - pos.x + (1 - stepX) / 2) / rayDir.x;
            } else {
                perpWallDist = (mapY - pos.y + (1 - stepY) / 2) / rayDir.y;
            }

            int lineHeigth = (int)(this.canvasHeight / perpWallDist);
            int drawStart = Math.max(0, -lineHeigth / 2 + this.canvasHeight / 2);
            int drawEnd = Math.min(this.canvasHeight - 1, lineHeigth / 2 + this.canvasHeight / 2);

            int texNum = worldMap[mapY][mapX] - 1;
            double wallX;
            if (side == 0) {
                wallX = pos.y + perpWallDist * rayDir.y;
            } else {
                wallX = pos.x + perpWallDist * rayDir.x;
            }

            wallX -= Math.floor(wallX);

            Texture t = textures[texNum];
            int texX = (int)(wallX * (double)(t.width));
            if ((side == 0 && rayDir.x > 0) ||
                (side == 1 && rayDir.y < 0)) {
                texX = t.width - texX - 1;
            }

            for (int y = drawStart; y < drawEnd; y++) {
                int d = (int)(y * 256 - this.canvasHeight * 128 + lineHeigth * 128);
                int texY = ((d * t.height) / lineHeigth) / 256;
                int c = t.getColor(texX, texY);
                buffer.setPixel(x, y, c);
            }
        }
    }

    private void udpate(double delta) {
        dir = Vec2.fromAngle(dir.toAngle() + 60 * delta);
        plane = Vec2.fromAngle(plane.toAngle() + 60 * delta).mul(plane.len());
    }

    private long lastNanoTime;
    final static int worldMap[][] = {
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,2,2,2,2,2,0,0,0,0,3,0,3,0,3,0,0,0,1},
        {1,0,0,0,0,0,2,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,2,0,0,0,2,0,0,0,0,3,0,0,0,3,0,0,0,1},
        {1,0,0,0,0,0,2,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,2,2,0,2,2,0,0,0,0,3,0,3,0,3,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,4,4,4,4,4,4,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,4,0,4,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,4,0,0,0,0,5,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,4,0,4,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,4,0,4,4,4,4,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,4,4,4,4,4,4,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
    };

    private Texture[] textures;
    private Backbuffer buffer;
    private GraphicsContext gc;
    private PixelWriter pw;


    private int canvasWidth;
    private int canvasHeight;

    private Vec2 pos;
    private Vec2 dir;
    private Vec2 plane;
}