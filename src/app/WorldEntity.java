package app;

public abstract class WorldEntity extends Entity {
    public WorldEntity(Vec2 pos, Vec2 dir, Vec2 vel) {
        super(pos, dir, vel);

        this.textureId = 0;
        this.solid = false;
    }

    public Sprite getSprite() {
        return new Sprite(this.position.copy(), this.textureId, this.solid);
    }

    protected int textureId;
    protected boolean solid;
}