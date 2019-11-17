package app;

public abstract class TileEntity extends Entity {
    public TileEntity(Vec2 pos) {
        super(pos, new Vec2(), new Vec2());
    }

    public boolean isSolid() {
        return false;
    }

    public abstract RayResult castRay(Vec2 start, Vec2 dir);
}