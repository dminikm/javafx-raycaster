package app;

import javafx.scene.media.AudioClip;

public class TurretEntity extends MonsterEntity {
    TurretEntity(Vec2 pos, AnimatedSprite animation, AnimatedSprite deadSprite, AudioClip hurtSound, AudioClip firingSound) {
        super(pos, new Vec2(), new Vec2());

        this.animation = animation;
        this.deadSprite = deadSprite;
        this.elapsedTime = 0;

        this.hurtSound = hurtSound;
        this.firingSound = firingSound;

        this.health = 45;
    }

    @Override
    public void update(double delta, World world) {
        this.elapsedTime += delta;

        if (this.health <= 0) {
            this.animation.setElapsedTime(0);
            return;
        }

        if (!firing) {
            Vec2 dir = world.getPlayer().getPosition().sub(this.position).normalize();
            EntityRaycastResult res = world.castRayEntity(this.position, dir, this);

            if (res.hit && res.entity instanceof Player) {
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

        if (timeDiff > 0.2 * (this.shotsFired + 1) && timeDiff < 0.7 && this.firing) {
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
        this.firingSound.play();

        // Alert other entities
        world.alertEntitiesInDistance(this.position, 4);
    }

    @Override
    public Sprite getSprite() {
        return (this.health > 0) ? this.animation.getSprite(this.position.copy(), false)
                                 : this.deadSprite.getSprite(this.position.copy(), false);
    }

    @Override
    public void takeDamage(double damage) {
        super.takeDamage(damage);

        if (!this.hurtSound.isPlaying()) {
            this.hurtSound.play();
        }
    }

    private AnimatedSprite animation;
    private AnimatedSprite deadSprite;
    private double elapsedTime;

    private double firingTime = 0;
    private boolean firing = false;
    private Vec2 target = new Vec2();
    private int shotsFired = 0;

    private final int damage = 5;

    AudioClip hurtSound;
    AudioClip firingSound;
}