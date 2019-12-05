package app;

import java.util.Random;
import javafx.scene.media.AudioClip;

class Weapon {
    public Weapon(
        String name,
        AudioClip sound,
        AnimatedSprite animation,
        int ammo, double delay,
        int numShots, double spread,
        int damage, double range,
        boolean available
        ) {
        this.name = name;
        this.sound = sound;
        this.available = available;
        this.ammo = ammo;
        this.animation = animation;
        this.fireDelay = delay;
        this.numShots = numShots;
        this.spread = spread;
        this.damage = damage;
        this.range = range;

        this.lastFired = -1;
        this.elapsedTime = 0;
    }

    public boolean isAvailable() {
        return this.available;
    }

    public void setAvailable(boolean av) {
        this.available = av;
    }

    public boolean isFiring() {
        return this.elapsedTime - this.fireDelay < this.lastFired;
    }

    public void fire(Vec2 pos, Vec2 dir, World world) {
        if (this.ammo == 0 || this.isFiring())
            return;

        Random r = new Random();
        r.nextDouble();

        this.ammo--;
        this.lastFired = this.elapsedTime;

        double startAngle = dir.toAngle() - ((this.numShots / 2) * this.spread);

        for (int i = 0; i < this.numShots; i++) {
            double angle = 
                startAngle +                                                        // Angle \|/ (the first one)
                (i * this.spread) +                                                 // Angle \|/ (the i-th one)
                ((r.nextInt(50) > 24) ? 1 : -1) * (r.nextDouble() * this.spread);   // Random \/ spread to the side

            EntityRaycastResult res = world.castRayEntity(pos, Vec2.fromAngle(angle), world.getPlayer());
            if (res.hit && res.distance <= this.range) {
                res.entity.takeDamage(this.damage);
            }
        }

        this.sound.play();

        world.alertEntitiesInDistance(pos, this.range);
    }

    public void update(double delta)
    {
        this.elapsedTime += delta;

        if (this.isFiring()) {
            this.animation.update(delta);
        } else {
            this.animation.setElapsedTime(0);
        }
    }

    public int getTexture() {
        return this.animation.getTextureId();
    }

    public String getName() {
        return this.name;
    }

    public int getAmmo() {
        return this.ammo;
    }

    public void addAmmo(int ammo) {
        this.ammo += ammo;
    }

    private boolean available;
    private AnimatedSprite animation;
    private int ammo;

    private double fireDelay;
    private double lastFired;
    private double elapsedTime;

    private int numShots;
    private double spread;
    private int damage;
    private double range;

    private String name;

    private AudioClip sound;
}