package app;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javafx.scene.input.KeyCode;

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
    private World(int[][] worldMap) {
        this.worldMap = worldMap;

        this.entities = new ArrayList<Entity>();
        this.tileEntities = new ArrayList<>();
        this.sprites = new ArrayList<Sprite>();
        this.tileEntityIds = new ArrayList<Integer>();

        this.tileEntities.add(new DoorTileEntity(new Vec2(10, 12), true));
        this.tileEntityIds.add(99);
    }

    private static int[][] parseLevel(JSONObject json) {
        List<List<Number>> level = JSONUtils.getFromComplexPath(json, "map.level");

        int[][] levelData = new int[level.size()][0];

        for (int i = 0; i < levelData.length; i++) {
            levelData[i] = new int[level.get(i).size()];
            for (int o = 0; o < levelData[i].length; o++) {
                levelData[i][o] = level.get(i).get(o).intValue();
            }
        }

        return levelData;
    }

    private static List<Sprite> parseSprites(JSONObject json) {
        List<JSONObject> sprites = JSONUtils.getFromComplexPath(json, "map.sprites");

        var parsedSprites = new ArrayList<Sprite>();
        for (JSONObject sprite : sprites) {
            Sprite spr = new Sprite();

            spr.pos = JSONUtils.vecFromJson(sprite, "position");
            spr.textureId = ((Number)sprite.get("textureId")).intValue();
            spr.solid = (boolean)sprite.get("solid");

            parsedSprites.add(spr);
        }

        return parsedSprites;
    }

    public static World fromFile(String fileName, Textureregistry textureregistry) {
        FileReader file;
        JSONObject json;
        try {
            file = new FileReader(fileName);
            json = (JSONObject)(new JSONParser().parse(file));
        } catch (Exception e) {
            json = new JSONObject();
        }

        textureregistry.loadTexturesFromJSON(json);
        
        var w = new World(parseLevel(json));
        w.setPlayer(Player.fromJSON(json, w));

        w.sprites = parseSprites(json);

        return w;
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

    private void setPlayer(Player p) {
        this.entities.add(p);
        this.player = p;
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