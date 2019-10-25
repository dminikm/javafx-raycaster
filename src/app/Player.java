package app;

public class Player extends Entity {
    public Player(Vec2 pos, Vec2 dir) {
        super(pos, dir, new Vec2(0, 0));
    }

    public Player(Vec2 pos, Vec2 dir, Vec2 vel) {
        super(pos, dir, vel);
    }

    @Override
    public void update(double delta) {
        this.position = this.position.add(this.velocity.mul(delta));
    }
}