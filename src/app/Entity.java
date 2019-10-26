package app;

public abstract class Entity {
    public Entity(Vec2 pos, Vec2 dir, Vec2 vel, World world) {
        this.position = pos;
        this.direction = dir;
        this.velocity = vel;
        this.world = world;
    }

    public Vec2 getPosition() {
        return this.position.copy();
    }

    public Vec2 getDirection() {
        return this.direction.copy();
    }

    public Vec2 getVelocity() {
        return this.velocity.copy();
    }

    public abstract void update(double delta);

    protected Vec2 position;
    protected Vec2 direction;
    protected Vec2 velocity;
    protected World world;
}