package app;

import java.util.HashMap;
import java.util.Map;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

enum KeyState {
    KeyStateUp,
    KeyStatePressed,
    KeyStateHeld
}

public class Keyregistry {
    private static Keyregistry instance = null;

    public static Keyregistry getInstance() {
        return instance;
    }

    public static Keyregistry constructInstance(Scene scene) {
        if (instance == null) {
            instance = new Keyregistry(scene);
        }

        return instance;
    }

    private Keyregistry(Scene scene) {
        this.keys = new HashMap<KeyCode, KeyState>();
        var self = this;

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

        return this.keys.get(key) != KeyState.KeyStateUp;
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
            this.keys.put(e.getCode(), KeyState.KeyStateUp);
        }
    }

    private Map<KeyCode, KeyState> keys;
}