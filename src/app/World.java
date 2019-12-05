package app;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

class Sprite {
    public Sprite(Vec2 pos, int textureId, boolean solid) {
        this.pos = pos;
        this.textureId = textureId;
        this.solid = solid;
    }

    public Vec2     pos;
    public int      textureId;
    public boolean  solid;
}

public class World implements RayCastable {
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

        this.toPlayerPaths = new Vec2[this.worldMap.length][this.worldMap[0].length];
    }

    public void resetTo(World w) {
        this.entities = w.entities;
        this.tileEntities = w.tileEntities;
        this.player = w.player;
        this.sprites = w.sprites;
        this.worldMap = w.worldMap;

        this.toPlayerPaths = new Vec2[this.worldMap.length][this.worldMap[0].length];
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

                    TileEntityRaycastResult res = (TileEntityRaycastResult)tent.castRay(pos, dir);
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

    public EntityRaycastResult castRayEntity(Vec2 start, Vec2 dir, Entity ignore) {
        double closestDistance = 99999;
        EntityRaycastResult result = new EntityRaycastResult();

        for (Entity ent : this.entities) {
            if (ent == ignore) { continue; }
            if (ent.health <= 0) { continue; }

            Rect bb = ent.getBoundingBox();
            RaycastResult res = bb.castRay(start, dir);

            if (res.hit && res.distance < closestDistance) {
                closestDistance = res.distance;
                result = new EntityRaycastResult(res);
                result.entity = ent;
            }
        }

        RaycastResult worldRes = this.castRay(start, dir);

        if (worldRes.distance < result.distance) {
            return new EntityRaycastResult();
        }

        return result;
    }

    public void interactRay(Vec2 start, Vec2 dir) {
        int mapX = (int)start.x;
        int mapY = (int)start.y;
        Vec2 sideDist = new Vec2();
        Vec2 deltaDist = new Vec2(Math.abs(1 / dir.x), Math.abs(1 / dir.y));
        int stepX;
        int stepY;
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

        while (true) {
            if (sideDist.x < sideDist.y) {
                sideDist.x += deltaDist.x;
                mapX += stepX;
                side = 0;
            } else {
                sideDist.y += deltaDist.y;
                mapY += stepY;
                side = 1;
            }

            if (this.worldMap[mapY][mapX] == -1) {
                TileEntity tent = this.getTileEntityAt(mapX, mapY);
                
                if (tent != null) {
                    double dist = 0;

                    if (side == 0) {
                        dist = (mapX - start.x + (1 - stepX) / 2) / dir.x;
                    } else {
                        dist = (mapY - start.y + (1 - stepY) / 2) / dir.y;
                    }

                    if (dist < 0.6) {
                        tent.onInteract();
                        return;
                    } 
                }
            }

            if (this.worldMap[mapY][mapX] != 0) {
                return;
            }
        }
    }

    public boolean isFree(Vec2 pos) {        
        if (this.worldMap[(int)pos.y][(int)pos.x] == -1) {
            TileEntity tent = this.getTileEntityAt((int)pos.x, (int)pos.y);

            if (tent != null && tent.isSolid()) {
                return false;
            }
        } else if (this.worldMap[(int)pos.y][(int)pos.x] == 0) { // Check for entities and sprites
            List<Sprite> sprites = this.getAllSprites();
            
            for (Sprite sprite : sprites) {
                if ((int)sprite.pos.x == (int)pos.x && (int)sprite.pos.y == (int)pos.y && sprite.solid) {
                    return false;
                }
            }
        } else {
            return false;
        }

        return true;
    }

    public boolean isFree(Vec2 pos, boolean ignoreEntities) {        
        if (this.worldMap[(int)pos.y][(int)pos.x] == -1) {
            TileEntity tent = this.getTileEntityAt((int)pos.x, (int)pos.y);

            if (tent != null && tent.isSolid()) {
                return false;
            }
        } else if (this.worldMap[(int)pos.y][(int)pos.x] == 0) { // Check for entities and sprites
            List<Sprite> sprites = (ignoreEntities) ? this.sprites : this.getAllSprites();
            
            for (Sprite sprite : sprites) {
                if ((int)sprite.pos.x == (int)pos.x && (int)sprite.pos.y == (int)pos.y && sprite.solid) {
                    return false;
                }
            }
        } else {
            return false;
        }

        return true;
    }

    public void update(double delta) {
        this.updatePlayerPaths();

        for (Entity ent : this.entities) {
            ent.update(delta, this);

            Vec2 pos = ent.getPosition();
            Vec2 vel = ent.getVelocity().mul(delta);
            Rect bb = ent.getBoundingBox();
            
            if (vel.x > 0 && (!this.isFree(new Vec2((int)pos.x + 1, (int)(pos.y - bb.h / 2))) ||
                !this.isFree(new Vec2((int)pos.x + 1, (int)(pos.y + bb.h / 2))))) {
                vel.x = Math.min(((int)pos.x + 1) - pos.x - bb.w, vel.x);
            } else if (vel.x < 0 && (!this.isFree(new Vec2((int)pos.x - 1, (int)(pos.y - bb.h / 2))) ||
                !this.isFree(new Vec2((int)pos.x - 1, (int)(pos.y + bb.h / 2))))) {
                vel.x = Math.max(((int)pos.x) - pos.x + bb.w, vel.x);
            }
            
            if (vel.y > 0 && (!this.isFree(new Vec2((int)(pos.x - bb.w / 2), (int)pos.y + 1)) || 
                !this.isFree(new Vec2((int)(pos.x + bb.w / 2), (int)pos.y + 1)))) {
                vel.y = Math.min(((int)pos.y + 1) - pos.y - bb.h, vel.y);
            } else if (vel.y < 0 && (!this.isFree(new Vec2((int)(pos.x - bb.w / 2), (int)pos.y - 1)) || 
                !this.isFree(new Vec2((int)(pos.x + bb.w / 2), (int)pos.y - 1)))) {
                vel.y = Math.max(((int)pos.y) - pos.y + bb.h, vel.y);
            }

            Vec2 newPos = ent.getPosition().add(vel);
            ent.setPosition(newPos);
        }

        for (TileEntity tent : this.tileEntities) {
            tent.update(delta, this);
        }

        this.entities = this.entities.stream().filter((final Entity ent) -> {
            return !ent.markedDelete();
        }).collect(Collectors.toList());
    }

    public Player getPlayer() {
        return (Player)this.player;
    }

    public List<Sprite> getAllSprites() {
        List<Sprite> sprites = new ArrayList<Sprite>(this.sprites);
        List<Sprite> entitySprites = this.entities.stream().filter((final Entity entity) -> {
            return entity instanceof WorldEntity;
        }).map((final Entity entity) -> {
            return ((WorldEntity)entity).getSprite();
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

    private boolean isInBounds(Vec2 pos) {
        return  pos.x >= 0 &&
                pos.y >= 0 &&
                pos.y < this.worldMap.length &&
                pos.x < this.worldMap[(int)pos.y].length;
    }

    private List<Vec2> getNeighborsForBlock(Vec2 pos) {
        List<Vec2> neighbors = new ArrayList<Vec2>(0);

        neighbors.add(pos.add(new Vec2(-1, 0)).floor());
        neighbors.add(pos.add(new Vec2(1, 0)).floor());
        neighbors.add(pos.add(new Vec2(0, -1)).floor());
        neighbors.add(pos.add(new Vec2(0, 1)).floor());

        return neighbors.stream().filter((final Vec2 position) -> {
            return this.isInBounds(position) && this.isFree(position, true);
        }).collect(Collectors.toList());
    }

    private void updatePlayerPaths() {
        // Clear paths
        for (int y = 0; y < this.toPlayerPaths.length; y++) {
            for (int x = 0; x < this.toPlayerPaths[y].length; x++) {
                this.toPlayerPaths[y][x] = null;
            }
        }

        Deque<Vec2> frontier = new ArrayDeque<Vec2>(); // New frontier
        Vec2 playerPos = this.getPlayer().getPosition().floor();
        frontier.push(playerPos); // Push player to frontier

        while (!frontier.isEmpty()) {
            Vec2 current = frontier.pop();
            for (Vec2 neighbor : this.getNeighborsForBlock(current)) {
                if (this.toPlayerPaths[(int)neighbor.y][(int)neighbor.x] == null) {     // Not updated yet
                    frontier.add(neighbor);                                             // Add as a next check
                    this.toPlayerPaths[(int)neighbor.y][(int)neighbor.x] = current;     // Set it's path to current
                }
            }
        }

        this.toPlayerPaths[(int)playerPos.y][(int)playerPos.x] = null;
    }

    public List<Vec2> getPathToPlayer(Vec2 pos) {
        List<Vec2> path = new ArrayList<Vec2>(0);
        if (this.toPlayerPaths[(int)pos.y][(int)pos.x] != null) {   // Can get to player
            Vec2 currentPos = pos.floor();
            while (!currentPos.eqi(this.getPlayer().getPosition())) {    // We havent reached the player
                Vec2 cameFrom = this.toPlayerPaths[(int)currentPos.y][(int)currentPos.x];   // Where this cell came from
                path.add(cameFrom.floor());
                currentPos = cameFrom.floor();
            }
        }

        return path;
    }

    public void alertEntitiesInDistance(Vec2 pos, double distance) {
        for (Entity ent : this.entities) {
            if (ent instanceof MonsterEntity) {
                if (ent.getPosition().sub(pos).len() < distance) {
                    ((MonsterEntity)ent).onAlert();
                }
            }
        }
    }

    private int[][] worldMap;

    private Entity              player;
    private List<Entity>        entities;
    private List<TileEntity>    tileEntities;
    private List<Sprite>        sprites;

    // Pathfinding stuff
    private Vec2[][] toPlayerPaths;
}
