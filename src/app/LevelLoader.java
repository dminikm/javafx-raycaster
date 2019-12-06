package app;

import java.util.ArrayList;
import java.util.List;
import java.io.FileReader;
import org.json.simple.parser.JSONParser;
import javafx.scene.media.AudioClip;
import org.json.simple.JSONObject;
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
            fileName,
            parseLevel(json),
            p,
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

    private static List<Sprite> parseSprites(JSONObject json) {
        List<JSONObject> sprites = JSONUtils.getFromComplexPath(json, "map.sprites");

        return sprites.stream().map((final JSONObject sprite) -> {
            return new Sprite(
                JSONUtils.vecFromJson(sprite, "position"),
                ((Number)sprite.get("textureId")).intValue(),
                (boolean)sprite.get("solid")
            );
        }).collect(Collectors.toList());
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

    private static List<Entity> parseEntities(JSONObject json) {
        List<JSONObject> entities = JSONUtils.getFromComplexPath(json, "map.entities");

        return entities.stream().map((final JSONObject entity) -> {
            String template = JSONUtils.getFromComplexPath(entity, "template");

            switch (template.toLowerCase())
            {
                case "turretentity":
                    return parseTurretEntity(entity);

                case "dogentity":
                    return parseDogEntity(entity);

                case "ammopickupentity":
                    return parseAmmoPickupEntity(entity);

                case "healthpickupentity":
                    return parseHealthPickupEntity(entity);

                default:
                    return null;
            }
        }).collect(Collectors.toList());
    }

    private static List<TileEntity> parseTileEntities(JSONObject json) {
        List<JSONObject> tileEntities = JSONUtils.getFromComplexPath(json, "map.tileEntities");

        return tileEntities.stream().map((final JSONObject tileEntity) -> {
            String template = JSONUtils.getFromComplexPath(tileEntity, "template");

            switch (template.toLowerCase())
            {
                case "doortileentity":
                    return parseDoorTileEntity(tileEntity);

                case "gameendtileentity":
                    return parseGameEndTileEntity(tileEntity);

                case "levelchangetileentity":
                    return parseLevelChangeTileEntity(tileEntity);

                case "secretdoortileentity":
                    return parseSecretDoorTileEntity(tileEntity);

                default:
                    return null;
            }
        }).collect(Collectors.toList());
    }

    // Individual tile entity parsers
    private static TileEntity parseDoorTileEntity(JSONObject json) {
        Vec2 position = JSONUtils.vecFromJson(json, "position");
        Vec2 startOffset = JSONUtils.vecFromJson(json, "startOffset");
        Vec2 endOffset = JSONUtils.vecFromJson(json, "endOffset");
        int textureId = ((Number)JSONUtils.getFromComplexPath(json, "textureId")).intValue();
        AudioClip sound = JSONUtils.getAudioClipFromJson(json, "sound");

        return new DoorTileEntity(position, startOffset, endOffset, textureId, sound);
    }

    private static TileEntity parseGameEndTileEntity(JSONObject json) {
        Vec2 position = JSONUtils.vecFromJson(json, "position");
        int textureId = ((Number)JSONUtils.getFromComplexPath(json, "textureId")).intValue();

        return new GameEndTileEntity(position, textureId);
    }

    private static TileEntity parseLevelChangeTileEntity(JSONObject json) {
        Vec2 position = JSONUtils.vecFromJson(json, "position");
        int textureId = ((Number)JSONUtils.getFromComplexPath(json, "textureId")).intValue();
        String nextLevel = JSONUtils.getFromComplexPath(json, "nextLevel");

        return new LevelChangeTileEntity(position, nextLevel, textureId);
    }

    private static TileEntity parseSecretDoorTileEntity(JSONObject json) {
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
    private static Entity parseTurretEntity(JSONObject json) {
        Vec2 position = JSONUtils.vecFromJson(json, "position");
        AnimatedSprite animation = JSONUtils.getAnimatedSpriteFromJson(json, "sprite.");
        AnimatedSprite deadSprite = JSONUtils.getAnimatedSpriteFromJson(json, "deadSprite.");
        AudioClip firingSound = JSONUtils.getAudioClipFromJson(json, "firingSound");
        AudioClip hurtSound = JSONUtils.getAudioClipFromJson(json, "hurtSound");

        return new TurretEntity(position, animation, deadSprite, hurtSound, firingSound);
    }

    private static Entity parseDogEntity(JSONObject json) {
        Vec2 position = JSONUtils.vecFromJson(json, "position");
        Vec2 direction = JSONUtils.vecFromJson(json, "direction");
        List<AnimatedSprite> sprites = JSONUtils.getAnimatedSpritesFromJson(json, "sprites");

        AudioClip hurtSound = JSONUtils.getAudioClipFromJson(json, "hurtSound");
        AudioClip attackSound = JSONUtils.getAudioClipFromJson(json, "attackSound");

        return new DogEntity(position, direction, sprites, hurtSound, attackSound);
    }

    private static Entity parseAmmoPickupEntity(JSONObject json) {
        Vec2 position = JSONUtils.vecFromJson(json, "position");
        int textureId = ((Number)JSONUtils.getFromComplexPath(json, "textureId")).intValue();
        int amount = ((Number)JSONUtils.getFromComplexPath(json, "amount")).intValue();
        String weaponName = JSONUtils.getFromComplexPath(json, "weaponName");
        boolean makeAvailable = JSONUtils.getFromComplexPath(json, "makeAvailable");

        return new AmmoPickupEntity(position, textureId, weaponName, amount, makeAvailable);
    }

    private static Entity parseHealthPickupEntity(JSONObject json) {
        Vec2 position = JSONUtils.vecFromJson(json, "position");
        int textureId = ((Number)JSONUtils.getFromComplexPath(json, "textureId")).intValue();
        int amount = ((Number)JSONUtils.getFromComplexPath(json, "amount")).intValue();

        return new HealthPickupEntity(position, textureId, amount);
    }
}