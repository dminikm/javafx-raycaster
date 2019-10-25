package app;

class RayResult {
    public boolean hit;
    public double distance;
    public int side;

    public Vec2 precisePositition;
    public Vec2 worldPositition;
}

enum RaycastMode {
    RaycastWorld(0x0001),
    RaycastEntity(0x0002),

    RaycastAll(0x0001 | 0x0002);

    private int value;    

    private RaycastMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

public class World {
    private World() {

    }

    public static World fromFile(String fileName) {
        return new World();
    }

    public RayResult castRay(RaycastMode mode, Vec2 start, Vec2 dir) {
        RayResult r = new RayResult();

        if ((mode.getValue() & RaycastMode.RaycastWorld.getValue()) >= 1 ||
            (mode.getValue() & RaycastMode.RaycastAll.getValue()) >= 1) {
            
            int mapX = (int)start.x;
            int mapY = (int)start.y;

            Vec2 sideDist = new Vec2();

            Vec2 deltaDist = new Vec2(Math.abs(1 / dir.x), Math.abs(1 / dir.y));
            double perpWallDist;

            int stepX;
            int stepY;

            int hit = 0;
            int side = 0;

            if (dir.x < 0) {
                stepX = -1;
                sideDist.x = (start.x - mapX) * deltaDist.x;
            } else {
                stepX = 1;
                sideDist.x = (mapX + 1.0 - start.x) * deltaDist.x;
            }

            if (dir.y < 0) {
                stepY = -1;
                sideDist.y = (start.y - mapY) * deltaDist.y;
            } else {
                stepY = 1;
                sideDist.y = (mapY + 1.0 - start.y) * deltaDist.y;
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
                perpWallDist = (mapX - start.x + (1 - stepX) / 2) / dir.x;
            } else {
                perpWallDist = (mapY - start.y + (1 - stepY) / 2) / dir.y;
            }

            r.distance = perpWallDist;
            r.hit = true;
            r.side = side;
            r.precisePositition = new Vec2(start.x + perpWallDist * dir.x, start.y + perpWallDist * dir.y);
            r.worldPositition = new Vec2(mapX, mapY);
        }

        return r;
    }

    public int getBlockFromRayResult(RayResult r) {
        return worldMap[(int)r.worldPositition.y][(int)r.worldPositition.x];
    }

    private final static int worldMap[][] = {
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
}