package app;

import java.util.ArrayList;
import java.util.List;
import java.io.FileReader;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class LevelLoader
{
    public static World loadLevel(String fileName, Textureregistry textureregistry) {
        FileReader file;
        JSONObject json;
        try {
            file = new FileReader(fileName);
            json = (JSONObject)(new JSONParser().parse(file));
        } catch (Exception e) {
            json = new JSONObject();
        }

        textureregistry.loadTexturesFromJSON(json);
        
        Integer[] tileEntityIDs = { 99 };
        TileEntity[] tileEntities = {};
        Entity[] entities = {};
        
        var w = new World(
            parseLevel(json),
            parsePlayer(json),
            tileEntityIDs,
            parseTileEntities(json),
            parseEntities(json),
            parseSprites(json)
        );

        return w;
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

    private static Sprite[] parseSprites(JSONObject json) {
        List<JSONObject> sprites = JSONUtils.getFromComplexPath(json, "map.sprites");

        var parsedSprites = new ArrayList<Sprite>();
        for (JSONObject sprite : sprites) {
            Sprite spr = new Sprite();

            spr.pos = JSONUtils.vecFromJson(sprite, "position");
            spr.textureId = ((Number)sprite.get("textureId")).intValue();
            spr.solid = (boolean)sprite.get("solid");

            parsedSprites.add(spr);
        }

        Sprite[] arr = new Sprite[parsedSprites.size()];
        parsedSprites.toArray(arr);
        return arr;
    }

    private static Player parsePlayer(JSONObject json) {
        var position = JSONUtils.vecFromJson(json, "player.start");
        var direction = JSONUtils.vecFromJson(json, "player.dir");

        return new Player(position, direction);
    }

    private static Entity[] parseEntities(JSONObject json) {
        return new Entity[0];
    }

    private static TileEntity[] parseTileEntities(JSONObject json) {
        List<JSONObject> tileEntities = JSONUtils.getFromComplexPath(json, "map.tileEntities");
        var parsedTileEntities = new ArrayList<TileEntity>();

        for (JSONObject tileEntity : tileEntities) {
            String template = JSONUtils.getFromComplexPath(tileEntity, "template");
            TileEntity tent = null;

            switch (template.toLowerCase())
            {
                case "doortileentity":
                    tent = parseDoorTileEntity(tileEntity);
                    break;

                default:
                    break;
            };

            parsedTileEntities.add(tent);
        }

        TileEntity[] res = new TileEntity[parsedTileEntities.size()];
        parsedTileEntities.toArray(res);

        return res;
    }

    // Individual tile entity parsers
    private static TileEntity parseDoorTileEntity(JSONObject json) {
        Vec2 position = JSONUtils.vecFromJson(json, "position");
        Vec2 startOffset = JSONUtils.vecFromJson(json, "startOffset");
        Vec2 endOffset = JSONUtils.vecFromJson(json, "endOffset");
        Number textureId = JSONUtils.getFromComplexPath(json, "textureId");

        return new DoorTileEntity(position, startOffset, endOffset, textureId.intValue());
    }
}