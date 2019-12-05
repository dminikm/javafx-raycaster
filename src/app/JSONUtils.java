package app;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

class JSONUtils {
    public static <T> T getFromComplexPath(JSONObject json, String path) {
        String[] nodes = path.split("\\.");

        Object currentJson = json;
        for (String node : nodes) {
            currentJson = ((JSONObject)currentJson).get(node);
        }

        return (T)currentJson;
    }

    public static Vec2 vecFromJson(JSONObject json, String path) {
        return new Vec2(
            ((Number)getFromComplexPath(json, path + ".x")).doubleValue(),
            ((Number)getFromComplexPath(json, path + ".y")).doubleValue()
        );
    }

    public static AnimatedSprite getAnimatedSpriteFromJson(JSONObject json, String path) {
        double cycleTime = ((Number)getFromComplexPath(json, path + "cycleTime")).doubleValue();
        List<Number> textureIds = getFromComplexPath(json, path + "textureIds");
        boolean repeat = getFromComplexPath(json, path + "repeat");

        return new AnimatedSprite(textureIds, cycleTime, repeat);
    }
}