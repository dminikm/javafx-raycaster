package app;

public abstract class TileEntity extends Entity {
    public TileEntity(Vec2 pos) {
        super(pos, new Vec2(), new Vec2());
    }

    public boolean isSolid() {
        return false;
    }

    public boolean canApplyShading() {
        return false;
    }

    public abstract TileEntityRaycastResult castRay(Vec2 start, Vec2 dir);
    public abstract int getTextureId();
}