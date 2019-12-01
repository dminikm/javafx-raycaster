package app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class RaycastResult {
    public boolean hit              = false;
    public double distance          = 0.0;
    public int side                 = 0;

    public double startOffset       = 0;
    public Vec2 precisePositition   = new Vec2();
    public Vec2 worldPositition     = new Vec2();
}

class TileEntityRaycastResult extends RaycastResult {
    public TileEntity entity = null;
}

class EntityRaycastResult extends RaycastResult {
    public Entity entity = null;
}

class BlockRaycastResult extends RaycastResult {
    public int blockId = 0;
}

class Sprite {
    public Vec2     pos;
    public int      textureId;
    public boolean  solid;
}

public class World {
    public World(int[][] worldMap, Player p, TileEntity[] tileEntities, Entity[] entities, Sprite[] sprites) {
        this.worldMap = worldMap;

        this.entities = new ArrayList<Entity>();
        this.tileEntities = new ArrayList<>();
        this.sprites = new ArrayList<Sprite>();

        Collections.addAll(this.entities, entities);
        Collections.addAll(this.tileEntities, tileEntities);
        Collections.addAll(this.sprites, sprites);

        this.player = p;
        this.entities.add(p);
    }

    public RaycastResult castRay(Vec2 start, Vec2 dir) {
        ArrayList<RaycastResult> results = new ArrayList<RaycastResult>();

        int mapX = (int)start.x;
        int mapY = (int)start.y;

        Vec2 sideDist = new Vec2();

        Vec2 deltaDist = new Vec2(Math.abs(1 / dir.x), Math.abs(1 / dir.y));
        double perpWallDist;

        int stepX;
        int stepY;

        boolean hit = false;
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

        while (!hit) {
            if (sideDist.x < sideDist.y) {
                sideDist.x += deltaDist.x;
                mapX += stepX;
                side = 0;
            } else {
                sideDist.y += deltaDist.y;
                mapY += stepY;
                side = 1;
            }

            if (this.worldMap[mapY][mapX] != 0) {
                if (this.worldMap[mapY][mapX] == -1) {
                    TileEntity tent = this.getTileEntityAt(mapX, mapY);
                    double dist = 0;

                    if (side == 0) {
                        dist = (mapX - start.x + (1 - stepX) / 2) / dir.x;
                    } else {
                        dist = (mapY - start.y + (1 - stepY) / 2) / dir.y;
                    }

                    Vec2 pos = start.add(dir.mul(dist));

                    TileEntityRaycastResult res = tent.castRay(pos, dir);
                    res.distance += dist;
                    res.worldPositition = new Vec2(mapX, mapY);
                    res.entity = tent;
                    res.side = side;

                    if (res.hit) {
                        results.add(res);
                        hit = false;
                    }

                } else {
                    hit = true;
                }
            }
        }

        if (this.worldMap[mapY][mapX] != -1) {
            if (side == 0) {
                perpWallDist = (mapX - start.x + (1 - stepX) / 2) / dir.x;
            } else {
                perpWallDist = (mapY - start.y + (1 - stepY) / 2) / dir.y;
            }
    
            BlockRaycastResult r = new BlockRaycastResult();
            r.hit = true;
            r.distance = perpWallDist;
            r.side = side;
            r.precisePositition = new Vec2(start.x + perpWallDist * dir.x, start.y + perpWallDist * dir.y);
            r.worldPositition = new Vec2(mapX, mapY);
            r.blockId = this.worldMap[mapY][mapX];
            
            if (r.side == 0) {
                r.startOffset = r.precisePositition.y - Math.floor(r.precisePositition.y);
            } else {
                r.startOffset = r.precisePositition.x - Math.floor(r.precisePositition.x);
            }
    
            results.add(r);
        }

        RaycastResult smallest = results.get(0);

        for (RaycastResult res : results) {
            if (res.distance < smallest.distance && !(res instanceof EntityRaycastResult)) {
                smallest = res;
            }
        }

        return smallest;
    }

    public boolean isFree(Vec2 pos) {
        // TODO: Don't do this
        return this.worldMap[(int)pos.y][(int)pos.x] == 0;
    }

    public void update(double delta) {
        for (Entity ent : this.entities) {
            ent.update(delta, this);

            Vec2 pos = ent.getPosition();
            Vec2 vel = ent.getVelocity().mul(delta);
            Rect bb = ent.getBoundingBox();
            
            if (vel.x > 0 && !this.isFree(new Vec2((int)pos.x + 1, (int)pos.y))) {
                vel.x = Math.min(((int)pos.x + 1) - pos.x - bb.w, vel.x);
            } else if (vel.x < 0 && !this.isFree(new Vec2((int)pos.x - 1, (int)pos.y))) {
                vel.x = Math.max(((int)pos.x) - pos.x + bb.w, vel.x);
            }
            
            if (vel.y > 0 && !this.isFree(new Vec2((int)pos.x, (int)pos.y + 1))) {
                vel.y = Math.min(((int)pos.y + 1) - pos.y - bb.h, vel.y);
            } else if (vel.y < 0 && !this.isFree(new Vec2((int)pos.x, (int)pos.y - 1))) {
                vel.y = Math.max(((int)pos.y) - pos.y + bb.h, vel.y);
            }

            Vec2 newPos = ent.getPosition().add(vel);
            ent.setPosition(newPos);
        }

        for (TileEntity tent : this.tileEntities) {
            tent.update(delta, this);
        }
    }

    public Entity getPlayer() {
        return this.player;
    }

    public List<Sprite> getAllSprites() {
        List<Sprite> sprites = new ArrayList(this.sprites);
        List<Sprite> entitySprites = this.entities.stream().filter((final Entity entity) -> {
            return entity instanceof MonsterEntity;
        }).map((final Entity entity) -> {
            return ((MonsterEntity)entity).getSprite();
        }).collect(Collectors.toList());

        sprites.addAll(entitySprites);
        return sprites;
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
}