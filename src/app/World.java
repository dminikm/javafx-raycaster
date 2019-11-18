package app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

class Sprite {
    public Vec2     pos;
    public int      textureId;
    public boolean  solid;
}

public class World {
    public World(int[][] worldMap, Player p, Integer[] tileEntityIDs, TileEntity[] tileEntities, Entity[] entities, Sprite[] sprites) {
        this.worldMap = worldMap;

        this.entities = new ArrayList<Entity>();
        this.tileEntities = new ArrayList<>();
        this.sprites = new ArrayList<Sprite>();
        this.tileEntityIds = new ArrayList<Integer>();

        Collections.addAll(this.entities, entities);
        Collections.addAll(this.tileEntities, tileEntities);
        Collections.addAll(this.sprites, sprites);
        Collections.addAll(this.tileEntityIds, tileEntityIDs);

        this.player = p;
        this.entities.add(p);
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

                if (this.worldMap[mapY][mapX] > 0) {
                    if (this.tileEntityIds.contains(this.worldMap[mapY][mapX])) {
                        TileEntity tent = this.getTileEntityAt(mapX, mapY);
                        double dist = 0;

                        if (side == 0) {
                            dist = (mapX - start.x + (1 - stepX) / 2) / dir.x;
                        } else {
                            dist = (mapY - start.y + (1 - stepY) / 2) / dir.y;
                        }

                        Vec2 pos = start.add(dir.mul(dist));

                        RayResult res = tent.castRay(pos, dir);
                        res.distance += dist;
                        res.worldPositition = new Vec2(1, 1);

                        if (res.hit) {
                            return res;
                        }

                    } else {
                        hit = 1;
                    }
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

    public boolean isFree(Vec2 pos) {
        // TODO: Don't do this
        return this.worldMap[(int)pos.y][(int)pos.x] == 0;
    }

    public int getBlockFromRayResult(RayResult r) {
        return this.worldMap[(int)r.worldPositition.y][(int)r.worldPositition.x];
    }

    public void update(double delta) {
        for (Entity ent : this.entities) {
            ent.update(delta);

            Vec2 pos = ent.getPosition();
            Vec2 vel = ent.getVelocity().mul(delta);
            Vec2 newPos = ent.getPosition().add(vel);

            Rect bbx = ent.getBoundingBox().move(vel);
            Rect bby = bbx.copy();

            bbx.y = pos.y;
            bby.x = pos.x;

            for (int i = 0; i < this.worldMap.length; i++) {
                for (int o = 0; o < this.worldMap[i].length; o++) {
                    if (!this.isFree(new Vec2(o, i))) {
                        Rect bbb = new Rect(o, i, 1, 1);


                        if (bbx.collidesWith(bbb)) {
                            newPos.x = pos.x;
                        }

                        if (bby.collidesWith(bbb)) {
                            newPos.y = pos.y;
                        }
                    }
                }
            }

            ent.setPosition(newPos);
        }

        for (TileEntity tent : this.tileEntities) {
            tent.update(delta);
        }
    }

    public Entity getPlayer() {
        return this.player;
    }

    public List<Sprite> getAllSprites() {
        return this.sprites;
    }

    public TileEntity getTileEntityAt(int x, int y) {
        for (TileEntity tent : this.tileEntities) {
            Vec2 pos = tent.getPosition();

            if ((int)pos.x == x && (int)pos.y == y)
                return tent;
        }

        return null;
    }

    private int[][] worldMap;

    private Entity              player;
    private List<Entity>        entities;
    private List<TileEntity>    tileEntities;
    private List<Sprite>        sprites;
    
    private List<Integer>       tileEntityIds;
}