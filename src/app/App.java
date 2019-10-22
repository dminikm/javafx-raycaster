package app;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class App extends Application {
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

    public static void main(String[] args) throws Exception
    {
        launch(args);
    }

    @Override
    public void start(Stage stage) 
    {
        stage.setTitle( "Canvas Example" );
        
        Group root = new Group();
        Scene scene = new Scene( root );
        stage.setScene( scene );
        
        final double width = 1280;
        final double height = 720;

        Canvas canvas = new Canvas( width, height );
        root.getChildren().add( canvas );
        
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        final long startNanoTime = System.nanoTime();

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

        new AnimationTimer()
        {
            public void handle(long currentNanoTime)
            {
                PixelWriter pw = gc.getPixelWriter();

                double delta = (currentNanoTime - startNanoTime) / 1000000000.0; 
                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, width, height);

                Vec2 pos = new Vec2(22, 12);
                Vec2 dir = new Vec2(-1, 0);
                Vec2 plane = new Vec2(0, 0.66);

                dir = Vec2.fromAngle(dir.toAngle() + 60 * delta);
                plane = Vec2.fromAngle(plane.toAngle() + 60 * delta);

                for (int x = 0; x < width; x++) {
                    double cameraX = 2 * x / width - 1;
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

                    int lineHeigth = (int)(height / perpWallDist);
                    int drawStart = Math.max(0, -lineHeigth / 2 + (int)height / 2);
                    int drawEnd = Math.min((int)height - 1, lineHeigth / 2 + (int)height / 2);

                    /*Color color;
                    switch (worldMap[mapY][mapX]) {
                        case 1: color = Color.RED; break;
                        case 2: color = Color.GREEN; break;
                        case 3: color = Color.BLUE; break;
                        case 4: color = Color.WHITE; break;
                        default: color = Color.YELLOW; break;
                    }

                    gc.setFill(color);
                    gc.setLineWidth(1.0);

                    gc.fillRect(x, drawStart, 1, lineHeigth);*/

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
                        int d = (int)(y * 256 - height * 128 + lineHeigth * 128);
                        int texY = ((d * t.height) / lineHeigth) / 256;
                        Color c = t.getColor(texX, texY);
                        
                        //gc.setFill(c);
                        //gc.setLineWidth(1.0);

                        //gc.fillRect(x, y, 1, 1);
                        pw.setColor(x, y, c);
                    }
                }
            }
        }.start();
        
        stage.show();
    }
}