package app;

import java.util.Map;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TextureRegistry {
    public TextureRegistry() {
        this.mapping = new HashMap<String, Integer>();
        this.textures = new ArrayList<Texture>();
    }

    public int getIdForTextureName(String name) {
        if (!this.mapping.containsKey(name)) {
            return -1;
        }

        return this.mapping.get(name);
    }

    public Texture getTextureForId(int id) {
        return this.textures.get(id);
    }

    // TODO:
    // This is intentionally bad
    // It should be improved and the level format re-thought
    public void loadTexturesFromJSON(JSONObject json) {
        this.dropTexures();

        List<JSONObject> textureList = JSONUtils.getFromComplexPath(json, "textures");

        for (JSONObject textureEntry : textureList) {
            int id = ((Number)JSONUtils.getFromComplexPath(textureEntry, "id")).intValue();
            String name = JSONUtils.getFromComplexPath(textureEntry, "name");

            this.mapping.put(name, id);

            while (this.textures.size() - 1 < id) {
                this.textures.add(null);
            }

            try { this.textures.set(id, Texture.from_image_path(name)); } catch(Exception e) {};
        }
    }

    private void dropTexures() {
        this.mapping = new HashMap<String, Integer>();
        this.textures = new ArrayList<Texture>();
    }

    private Map<String, Integer>    mapping;
    private List<Texture>           textures;
}