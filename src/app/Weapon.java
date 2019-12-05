package app;

class Weapon {
    public Weapon(
        String name,
        AnimatedSprite animation,
        int ammo, double delay,
        int numShots, double spread,
        int damage, double range,
        boolean available
        ) {
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

        this.ammo--;
        this.lastFired = this.elapsedTime;

        EntityRaycastResult res = world.castRayEntity(pos, dir, world.getPlayer());
        if (res.hit && res.distance <= this.range) {
            res.entity.takeDamage(this.damage);
        }
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
}