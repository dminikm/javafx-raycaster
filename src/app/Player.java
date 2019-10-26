package app;

import org.json.simple.JSONObject;

import javafx.scene.input.KeyCode;

public class Player extends Entity {
    public Player(Vec2 pos, Vec2 dir, World world) {
        super(pos, dir, new Vec2(0, 0), world);
    }

    public Player(Vec2 pos, Vec2 dir, Vec2 vel, World world) {
        super(pos, dir, vel, world);
    }

    @Override
    public void update(double delta) {
        Keyregistry r = Keyregistry.getInstance();

        final int speed = 16;

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
            svel = Vec2.fromAngle(this.direction.toAngle() + 90).mul(speed);
        } else if (r.isKeyDown(KeyCode.D)) {
            svel = Vec2.fromAngle(this.direction.toAngle() + 90).mul(-speed);
        } else {
            svel = new Vec2();
        }

        if (r.isKeyDown(KeyCode.LEFT)) {
            this.direction = Vec2.fromAngle(this.direction.toAngle() + 100 * delta);
        } else if (r.isKeyDown(KeyCode.RIGHT)) {
            this.direction = Vec2.fromAngle(this.direction.toAngle() - 100 * delta);
        }

        var mouseDelta = r.getMouseDeltaSmooth();
        this.direction = Vec2.fromAngle(this.direction.toAngle() + -mouseDelta.x * 7 * delta);

        this.velocity = fvel.add(svel);

        Vec2 newPos = this.position.add(this.velocity.mul(delta));
        if (this.world.isFree(newPos)) {
            this.position = newPos;
        }
    }

    public static Player fromJSON(JSONObject json, World world) {
        var position = JSONUtils.vecFromJson(json, "player.start");
        var direction = JSONUtils.vecFromJson(json, "player.dir");

        return new Player(position, direction, world);
    }
}