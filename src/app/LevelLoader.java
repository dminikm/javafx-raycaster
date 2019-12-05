package app;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;

import org.json.simple.parser.JSONParser;

import javafx.scene.media.AudioClip;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LevelLoader
{
    public static World loadLevel(String fileName, TextureRegistry textureregistry) {
        FileReader file;
        JSONObject json;
        try {
            file = new FileReader(fileName);
            json = (JSONObject)(new JSONParser().parse(file));
        } catch (Exception e) {
            json = new JSONObject();
        }

        textureregistry.loadTexturesFromJSON(json);
        
        Player p = parsePlayer(json);

        var w = new World(
            parseLevel(json),
            p,
            parseTileEntities(json, p),
            parseEntities(json, p),
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
            parsedSprites.add(new Sprite(
                JSONUtils.vecFromJson(sprite, "position"),
                ((Number)sprite.get("textureId")).intValue(),
                (boolean)sprite.get("solid")
            ));
        }

        Sprite[] arr = new Sprite[parsedSprites.size()];
        parsedSprites.toArray(arr);
        return arr;
    }

    private static Weapon parseWeapon(JSONObject json) {
        String name = JSONUtils.getFromComplexPath(json, "name");
        boolean available = JSONUtils.getFromComplexPath(json, "available");
        int ammo = ((Number)JSONUtils.getFromComplexPath(json, "ammo")).intValue();
        double fireDelay = ((Number)JSONUtils.getFromComplexPath(json, "fireDelay")).doubleValue();
        int numShots = ((Number)JSONUtils.getFromComplexPath(json, "numShots")).intValue();
        double spread = ((Number)JSONUtils.getFromComplexPath(json, "spread")).doubleValue();
        int damage = ((Number)JSONUtils.getFromComplexPath(json, "damage")).intValue();
        double range = ((Number)JSONUtils.getFromComplexPath(json, "range")).doubleValue();
        AnimatedSprite animation = JSONUtils.getAnimatedSpriteFromJson(json, "animation.");
        AudioClip sound = JSONUtils.getAudioClipFromJson(json, "sound");

        return new Weapon(name, sound, animation, ammo, fireDelay, numShots, spread, damage, range, available);
    }

    private static List<Weapon> parseWeapons(JSONObject json) {
        List<Weapon> weapons = new ArrayList<Weapon>(0);
        List<JSONObject> jsonWeapons = JSONUtils.getFromComplexPath(json, "weapons");

        weapons = jsonWeapons.stream().map((JSONObject weapon) -> {
            return parseWeapon(weapon);
        }).collect(Collectors.toList());

        return weapons;
    }

    private static Player parsePlayer(JSONObject json) {
        var position = JSONUtils.vecFromJson(json, "player.start");
        var direction = JSONUtils.vecFromJson(json, "player.dir");
        List<Weapon> weapons = parseWeapons(JSONUtils.getFromComplexPath(json, "player"));
        AudioClip hurtSound = JSONUtils.getAudioClipFromJson(json, "player.hurtSound");

        return new Player(position, direction, new Vec2(), weapons, hurtSound);
    }

    private static Entity[] parseEntities(JSONObject json, Player p) {
        List<JSONObject> entities = JSONUtils.getFromComplexPath(json, "map.entities");
        var parsedEntities = new ArrayList<Entity>();

        for (JSONObject entity : entities) {
            String template = JSONUtils.getFromComplexPath(entity, "template");
            Entity ent = null;

            switch (template.toLowerCase())
            {
                case "turretentity":
                    ent = parseTurretEntity(entity, p);
                    break;

                case "dogentity":
                    ent = parseDogEntity(entity, p);
                    break;

                case "ammopickupentity":
                    ent = parseAmmoPickupEntity(entity, p);
                    break;

                case "healthpickupentity":
                    ent = parseHealthPickupEntity(entity, p);
                    break;

                default:
                    break;
            };

            parsedEntities.add(ent);
        }

        Entity[] res = new Entity[parsedEntities.size()];
        parsedEntities.toArray(res);

        return res;
    }

    private static TileEntity[] parseTileEntities(JSONObject json, Player p) {
        List<JSONObject> tileEntities = JSONUtils.getFromComplexPath(json, "map.tileEntities");
        var parsedTileEntities = new ArrayList<TileEntity>();

        for (JSONObject tileEntity : tileEntities) {
            String template = JSONUtils.getFromComplexPath(tileEntity, "template");
            TileEntity tent = null;

            switch (template.toLowerCase())
            {
                case "doortileentity":
                    tent = parseDoorTileEntity(tileEntity, p);
                    break;

                case "gameendtileentity":
                    tent = parseGameEndTileEntity(tileEntity, p);
                    break;

                case "levelchangetileentity":
                    tent = parseLevelChangeTileEntity(tileEntity, p);
                    break;

                case "secretdoortileentity":
                    tent = parseSecretDoorTileEntity(tileEntity, p);
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
    private static TileEntity parseDoorTileEntity(JSONObject json, Player p) {
        Vec2 position = JSONUtils.vecFromJson(json, "position");
        Vec2 startOffset = JSONUtils.vecFromJson(json, "startOffset");
        Vec2 endOffset = JSONUtils.vecFromJson(json, "endOffset");
        int textureId = ((Number)JSONUtils.getFromComplexPath(json, "textureId")).intValue();
        AudioClip sound = JSONUtils.getAudioClipFromJson(json, "sound");

        return new DoorTileEntity(position, startOffset, endOffset, textureId, sound);
    }

    private static TileEntity parseGameEndTileEntity(JSONObject json, Player p) {
        Vec2 position = JSONUtils.vecFromJson(json, "position");
        int textureId = ((Number)JSONUtils.getFromComplexPath(json, "textureId")).intValue();

        return new GameEndTileEntity(position, textureId);
    }

    private static TileEntity parseLevelChangeTileEntity(JSONObject json, Player p) {
        Vec2 position = JSONUtils.vecFromJson(json, "position");
        int textureId = ((Number)JSONUtils.getFromComplexPath(json, "textureId")).intValue();
        String nextLevel = JSONUtils.getFromComplexPath(json, "nextLevel");

        return new LevelChangeTileEntity(position, nextLevel, textureId);
    }

    private static TileEntity parseSecretDoorTileEntity(JSONObject json, Player p) {
        Vec2 position = JSONUtils.vecFromJson(json, "position");
        int textureId = ((Number)JSONUtils.getFromComplexPath(json, "textureId")).intValue();

        List<JSONObject> pathJSON = JSONUtils.getFromComplexPath(json, "path");
        List<Vec2> path = pathJSON.stream().map((final JSONObject vec) -> {
            return new Vec2(
                ((Number)JSONUtils.getFromComplexPath(vec, "x")).doubleValue(),
                ((Number)JSONUtils.getFromComplexPath(vec, "y")).doubleValue()
            );
        }).collect(Collectors.toList());

        return new SecretDoorTileEntity(position, textureId, path);
    }

    // Individual entity parsers
    private static Entity parseTurretEntity(JSONObject json, Player p) {
        Vec2 position = JSONUtils.vecFromJson(json, "position");
        AnimatedSprite animation = JSONUtils.getAnimatedSpriteFromJson(json, "sprite.");
        AnimatedSprite deadSprite = JSONUtils.getAnimatedSpriteFromJson(json, "deadSprite.");
        AudioClip firingSound = JSONUtils.getAudioClipFromJson(json, "firingSound");
        AudioClip hurtSound = JSONUtils.getAudioClipFromJson(json, "hurtSound");

        return new TurretEntity(position, p, animation, deadSprite, hurtSound, firingSound);
    }

    private static Entity parseDogEntity(JSONObject json, Player p) {
        Vec2 position = JSONUtils.vecFromJson(json, "position");
        Vec2 direction = JSONUtils.vecFromJson(json, "direction");
        List<AnimatedSprite> sprites = JSONUtils.getAnimatedSpritesFromJson(json, "sprites");

        AudioClip hurtSound = JSONUtils.getAudioClipFromJson(json, "hurtSound");
        AudioClip attackSound = JSONUtils.getAudioClipFromJson(json, "attackSound");

        return new DogEntity(position, direction, p, sprites, hurtSound, attackSound);
    }

    private static Entity parseAmmoPickupEntity(JSONObject json, Player p) {
        Vec2 position = JSONUtils.vecFromJson(json, "position");
        int textureId = ((Number)JSONUtils.getFromComplexPath(json, "textureId")).intValue();
        int amount = ((Number)JSONUtils.getFromComplexPath(json, "amount")).intValue();

        return new AmmoPickupEntity(position, textureId, amount);
    }

    private static Entity parseHealthPickupEntity(JSONObject json, Player p) {
        Vec2 position = JSONUtils.vecFromJson(json, "position");
        int textureId = ((Number)JSONUtils.getFromComplexPath(json, "textureId")).intValue();
        int amount = ((Number)JSONUtils.getFromComplexPath(json, "amount")).intValue();

        return new HealthPickupEntity(position, textureId, amount);
    }
}