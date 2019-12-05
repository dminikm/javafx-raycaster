package app;

import java.util.HashMap;
import java.util.List;

import javafx.scene.media.AudioClip;

public class TurretEntity extends MonsterEntity {
    TurretEntity(Vec2 pos, Player player, AnimatedSprite animation, AudioClip sound) {
        super(pos, new Vec2(), new Vec2(), player);

        this.animation = animation;
        this.elapsedTime = 0;

        this.sound = sound;
    }

    @Override
    public void update(double delta, World world) {
        this.elapsedTime += delta;

        if (!firing) {
            Vec2 dir = world.getPlayer().getPosition().sub(this.position).normalize();
            RaycastResult res = world.castRayEntity(this.position, dir, this);

            if (res.hit) {
                this.firing = true;
                this.firingTime = this.elapsedTime;
                this.target = world.getPlayer().getPosition().copy();
            }
        }

        double timeDiff = this.elapsedTime - this.firingTime;
        if (this.firing && timeDiff > 1) {
            this.firing = false;
            this.firingTime = 0;
            this.shotsFired = 0;
        }

        if (timeDiff > 0.2 * (this.shotsFired + 1) && timeDiff < 0.7) {
            this.shotsFired++;
            this.fire(world);
        }

        if (this.firing) {
            this.animation.update(delta);
        } else {
            this.animation.setElapsedTime(0);
        }
    }

    private void fire(World world) {
        Vec2 dir = this.target.sub(this.position).normalize();
        EntityRaycastResult res = world.castRayEntity(this.position, dir, this);

        if (res.hit) {
            res.entity.takeDamage(this.damage);
        }

        // queue sound
        this.sound.play();

        // Alert other entities
        world.alertEntitiesInDistance(this.position, 4);
    }

    @Override
    public Sprite getSprite() {
        return this.animation.getSprite(this.position.copy(), false);
    }

    private AnimatedSprite animation;
    private double elapsedTime;

    private double firingTime = 0;
    private boolean firing = false;
    private Vec2 target = new Vec2();
    private int shotsFired = 0;

    private final int damage = 5;

    AudioClip sound;
}