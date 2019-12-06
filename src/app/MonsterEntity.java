package app;

public abstract class MonsterEntity extends WorldEntity {
    MonsterEntity(Vec2 pos, Vec2 dir, Vec2 vel) {
        super(pos, dir, vel);
        this.alerted = false;
    }

    public void onAlert() {
        this.alerted = true;
    }

    protected boolean alerted;
}