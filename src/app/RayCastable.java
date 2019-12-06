package app;

class RaycastResult {
    public RaycastResult() {}

    public RaycastResult(RaycastResult res) {
        this.hit = res.hit;
        this.distance = res.distance;
        this.side = res.side;
        this.startOffset = res.startOffset;
        this.precisePosition = res.precisePosition;
        this.worldPositition = res.worldPositition;
    }

    public boolean hit              = false;
    public double distance          = Double.MAX_VALUE;
    public int side                 = 0;

    public double startOffset       = 0;
    public Vec2 precisePosition   = new Vec2();
    public Vec2 worldPositition     = new Vec2();
}

class TileEntityRaycastResult extends RaycastResult {
    public TileEntityRaycastResult() {}
    public TileEntityRaycastResult(RaycastResult res) { super(res); }

    public TileEntity entity = null;
}

class EntityRaycastResult extends RaycastResult {
    public EntityRaycastResult() {}
    public EntityRaycastResult(RaycastResult res) { super(res); }

    public Entity entity = null;
}

class BlockRaycastResult extends RaycastResult {
    public int blockId = 0;
}

public interface RayCastable {
    public RaycastResult castRay(Vec2 start, Vec2 dir);
}