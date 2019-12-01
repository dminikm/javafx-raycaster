package app;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

enum KeyState {
    KeyStateUp,
    KeyStateReleased,
    KeyStatePressed,
    KeyStateHeld
}

public class KeyRegistry {
    private static KeyRegistry instance = null;

    public static KeyRegistry getInstance() {
        return instance;
    }

    public static KeyRegistry constructInstance(Scene scene) {
        if (instance == null) {
            instance = new KeyRegistry(scene);
        }

        return instance;
    }

    private KeyRegistry(Scene scene) {
        this.keys = new HashMap<KeyCode, KeyState>();
        var self = this;

        this.mouseDeltas = new ArrayDeque<Vec2>();
        this.mousePosition = new Vec2();

        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                self.handle(e, true);
            }
        });

        scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                self.handle(e, false);
            }
        });
    }

    public boolean isKeyDown(KeyCode key) {
        if (!this.keys.containsKey(key)) {
            this.keys.put(key, KeyState.KeyStateUp);
        }

        var keyState = this.keys.get(key);
        return keyState != KeyState.KeyStateUp && keyState != KeyState.KeyStateReleased;
    }

    public boolean hasKeyBeenReleased(KeyCode key) {
        if (!this.keys.containsKey(key)) {
            this.keys.put(key, KeyState.KeyStateUp);
        }

        return this.keys.get(key) == KeyState.KeyStateReleased;
    }

    public KeyState getKeyState(KeyCode key) {
        if (!this.keys.containsKey(key)) {
            this.keys.put(key, KeyState.KeyStateUp);
        }

        return this.keys.get(key);
    }


    public void handle(KeyEvent e, boolean press) {
        if (press) {
            if (!this.keys.containsKey(e.getCode()) || this.keys.get(e.getCode()) == KeyState.KeyStateUp) {
                this.keys.put(e.getCode(), KeyState.KeyStatePressed);
            } else {
                this.keys.put(e.getCode(), KeyState.KeyStateHeld);
            }
        } else {
            this.keys.put(e.getCode(), KeyState.KeyStateReleased);
        }
    }

    public Vec2 getMouseDelta() {
        return this.mouseDeltas.peekLast().copy();
    }

    public Vec2 getMouseDeltaSmooth() {
        Vec2 sum = new Vec2();
        for (Vec2 delta : this.mouseDeltas) {
            sum = sum.add(delta);
        }

        return sum.div(this.mouseDeltas.size() + 1);
    }

    public Vec2 getMousePosition() {
        return this.mousePosition.copy();
    }

    public void handleMouse(Vec2 delta, Vec2 position) {
        if (this.mouseDeltas.size() > 10) {
            this.mouseDeltas.pop();
        }

        this.mouseDeltas.add(delta.copy());
        this.mousePosition = position.copy();
    }

    public void update(double delta) {
        for (Map.Entry<KeyCode, KeyState> key : this.keys.entrySet()) {
            if (key.getValue() == KeyState.KeyStateReleased) {
                this.keys.put(key.getKey(), KeyState.KeyStateUp);
            }
        }
    }

    private Map<KeyCode, KeyState> keys;
    private Vec2 mousePosition;
    private Deque<Vec2> mouseDeltas;
}