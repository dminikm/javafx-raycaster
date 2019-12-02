package app;

public abstract class PickupEntity extends WorldEntity {
    public PickupEntity(Vec2 pos) {
        super(pos, new Vec2(), new Vec2());
        this.radius = 0;
    }

    @Override
    public void update(double delta, World world) {
        Entity p = world.getPlayer();
        double distance = p.getPosition().sub(this.position).len();

        if (distance < this.radius) {
            this.onPickup((Player)world.getPlayer());
        }
    }

    protected abstract void onPickup(Player p);

    protected double radius;
}