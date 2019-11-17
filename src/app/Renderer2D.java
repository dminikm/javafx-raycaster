package app;

import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Renderer2D {
    public Renderer2D(int width, int height, World world, Textureregistry registry, GraphicsContext gc) {
        this.internalWidth = width;
        this.internalHeight = height;

        this.world = world;
        this.textureRegistry = registry;

        this.gc = gc;
    }

    public void render(double delta) {
        int sizeX = 32;//this.internalWidth / this.world.getWidth();
        int sizeY = 32;//this.internalHeight / this.world.getHeight();
        
        for (int y = 0; y < this.world.getHeight(); y++) {
            for (int x = 0; x < this.world.getWidth(); x++) {
                if (world.getBlockAt(x, y) > 0) {
                    this.gc.fillRect(x * sizeX, y * sizeY, sizeX, sizeY);
                }
            }
        }

        Entity p = this.world.getPlayer();

        this.gc.fillRect((p.position.x * sizeX) - 5, (p.position.y * sizeY) - 5, 10, 10);
        
        for (TileEntity ent : this.world.getTileEntities()) {
            if (ent instanceof DoorTileEntity) {
                gc.setStroke(Color.BLUE);

                Vec2 p1 = ((DoorTileEntity)ent).getPoint1();
                Vec2 p2 = ((DoorTileEntity)ent).getPoint2();

                gc.beginPath();

                gc.moveTo(p1.x * sizeX, p1.y * sizeY);
                gc.lineTo(p2.x * sizeX, p2.y * sizeY);

                gc.stroke();

                gc.setStroke(Color.BLACK);
            }
        }

        Vec2 dir = this.world.getPlayer().getDirection();
        Vec2 pos = this.world.getPlayer().getPosition();
        Vec2 plane = this.getPlane();
        

        for (int x = (this.internalWidth / 2) - 0; x < (this.internalWidth / 2) + 1; x++) {
            double cameraX = 2 * x / (double)this.internalWidth - 1;
            Vec2 rayDir = dir.add(plane.mul(cameraX));
            rayDir = rayDir.normalize();

            RayResult res = this.world.castRay(RaycastMode.RaycastWorld, pos, rayDir);

            Vec2 end = pos.add(rayDir.mul(res.distance));

            gc.setStroke( Color.RED );

            //gc.beginPath();
            //gc.moveTo((p.position.x * sizeX), (p.position.y * sizeY));
            //gc.lineTo((end.x * sizeX), (end.y * sizeY));
            //gc.stroke();

            gc.setStroke( Color.BLACK );

            gc.setFill( Color.GREEN );
            gc.fillRect(res.precisePositition.x * sizeX, res.precisePositition.y * sizeY, 1, 1);

            for (Vec2 spot : res.raySteps) {
                gc.fillRect(spot.x * sizeX, spot.y * sizeY, 1, 1);
            }

            gc.setFill( Color.BLACK );

        }
    }

    public void renderPart(double delta, int start, int end) {
        /*buffer.clearRect(start, 0, end - start, buffer.getHeight());

        Vec2 dir = this.world.getPlayer().getDirection();
        Vec2 pos = this.world.getPlayer().getPosition();
        Vec2 plane = this.getPlane();
        

        for (int x = start; x < end; x++) {
            double cameraX = 2 * x / (double)this.internalWidth - 1;
            Vec2 rayDir = dir.add(plane.mul(cameraX));

            RayResult res = this.world.castRay(RaycastMode.RaycastWorld, pos, rayDir);

            int lineHeigth = (int)(this.internalHeight / res.distance);
            int drawStart = Math.max(0, -lineHeigth / 2 + this.internalHeight / 2);
            int drawEnd = Math.min(this.internalHeight - 1, lineHeigth / 2 + this.internalHeight / 2);

            double wallX;
            if (res.side == 0) {
                wallX = pos.y + res.distance * rayDir.y;
            } else {
                wallX = pos.x + res.distance * rayDir.x;
            }

            wallX -= Math.floor(wallX);

            Texture t = this.textureRegistry.getTextureForId(this.world.getBlockFromRayResult(res));
            int texX = (int)(wallX * (double)(t.width));
            if ((res.side == 0 && rayDir.x > 0) ||
                (res.side == 1 && rayDir.y < 0)) {
                texX = t.width - texX - 1;
            }

            for (int y = drawStart; y < drawEnd; y++) {
                int d = (int)(y * 256 - this.internalHeight * 128 + lineHeigth * 128);
                int texY = ((d * t.height) / lineHeigth) / 256;
                int c = t.getPixel(texX, texY);
                
                if(res.side == 1) c = 0xFF000000 | (((c & 0xFFFFFF) >>> 1) & 8355711);

                buffer.setPixel(x, y, c);
            }

            this.zBuffer[x] = res.distance;
        }*/
    }



    private Vec2 getPlane() {
        Vec2 dir = this.world.getPlayer().getDirection();
        return Vec2.fromAngle(dir.toAngle() - 90).mul(0.66);
    }

    private int internalWidth;
    private int internalHeight;

    private World world;
    private Textureregistry textureRegistry;

    private GraphicsContext gc;

}