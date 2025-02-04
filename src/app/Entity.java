package app;

public abstract class Entity {
    public Entity(Vec2 pos, Vec2 dir, Vec2 vel) {
        this.position = pos;
        this.direction = dir;
        this.velocity = vel;
        this.health = 100;

        this.delete = false;
    }

    public Vec2 getPosition() {
        return this.position.copy();
    }

    public void setPosition(Vec2 pos) {
        this.position = pos.copy();
    }

    public Vec2 getDirection() {
        return this.direction.copy();
    }
    
    public void setDirection(Vec2 dir) {
        this.direction = dir.copy();
    }

    public Vec2 getVelocity() {
        return this.velocity.copy();
    }

    public void setVelocity(Vec2 vel) {
        this.velocity = vel.copy();
    }

    public Rect getBoundingBox() {
        Rect r = new Rect();
        r.w = 0.4;
        r.h = 0.4;
        r.x = this.position.x - 0.2;
        r.y = this.position.y - 0.2;

        return r;
    }

    public void takeDamage(double damage) {
        this.health = Math.min(100, Math.max(0, this.health - damage));
    }

    public void onInteract() {}
    public abstract void update(double delta, World world);

    public final boolean markedDelete() {
        return this.delete;
    }

    protected Vec2 position;
    protected Vec2 direction;
    protected Vec2 velocity;
    protected double health;

    protected boolean delete;
}