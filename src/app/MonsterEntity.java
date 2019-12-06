package app;

public abstract class MonsterEntity extends WorldEntity {
    MonsterEntity(Vec2 pos, Vec2 dir, Vec2 vel) {
        super(pos, dir, vel);
        this.alerted = false;
    }

    public void onAlert() {
        this.alerted = true;
    }

    protected int getAngleToPlayer(World world) {
        Vec2 playerDir = world.getPlayer().getPosition().sub(this.position);
        double angle = Angle.normalizeDeg(playerDir.normalize().toAngle() - this.direction.toAngle());

        if (angle > 180 - 45 && angle < 180 + 45) {
            return 3;    // Back
        } else if (angle > 360 - 45 || angle < 0 + 45) {
            return 0;    // Front
        } else if (angle > 90 - 45 && angle < 90 + 45) {
            return 2;    // Right
        } else {
            return 1;    // Left
        }
    }

    protected boolean alerted;
}