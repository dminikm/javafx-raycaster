package app;

import org.json.simple.JSONObject;

import javafx.scene.input.KeyCode;

public class Player extends Entity {
    public Player(Vec2 pos, Vec2 dir) {
        super(pos, dir, new Vec2(0, 0));
    }

    public Player(Vec2 pos, Vec2 dir, Vec2 vel) {
        super(pos, dir, vel);
    }

    @Override
    public void update(double delta, World world) {
        KeyRegistry r = KeyRegistry.getInstance();

        int speed = 4;

        if (r.isKeyDown(KeyCode.SHIFT)) {
            speed *= 1.8;
        }

        Vec2 fvel;
        if (r.isKeyDown(KeyCode.W)) {
            fvel = Vec2.fromAngle(this.direction.toAngle()).mul(speed);
        } else if (r.isKeyDown(KeyCode.S)) {
            fvel = Vec2.fromAngle(this.direction.toAngle()).mul(-speed);
        } else {
            fvel = new Vec2();
        }

        Vec2 svel;
        if (r.isKeyDown(KeyCode.A)) {
            svel = Vec2.fromAngle(this.direction.toAngle() + 90).mul(-speed);
        } else if (r.isKeyDown(KeyCode.D)) {
            svel = Vec2.fromAngle(this.direction.toAngle() + 90).mul(speed);
        } else {
            svel = new Vec2();
        }

        if (r.isKeyDown(KeyCode.LEFT)) {
            this.direction = Vec2.fromAngle(this.direction.toAngle() - 100 * delta);
        } else if (r.isKeyDown(KeyCode.RIGHT)) {
            this.direction = Vec2.fromAngle(this.direction.toAngle() + 100 * delta);
        }

        var mouseDelta = r.getMouseDeltaSmooth();
        //this.direction = Vec2.fromAngle(this.direction.toAngle() + mouseDelta.x * 7 * delta);

        this.velocity = fvel.add(svel);
    }

    @Override
    public Rect getBoundingBox() {
        return new Rect(
            this.position.x - 0.1,
            this.position.y - 0.1,
            0.2,
            0.2
        );
   }
}
