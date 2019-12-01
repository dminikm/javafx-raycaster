package app;

public abstract class MonsterEntity extends Entity {
    MonsterEntity(Vec2 pos, Vec2 dir, Vec2 vel, Player player) {
        super(pos, dir, vel);
    }

    public abstract Sprite getSprite();
}