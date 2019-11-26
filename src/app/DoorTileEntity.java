package app;

public class DoorTileEntity extends TileEntity {
    public DoorTileEntity(Vec2 pos, Vec2 startOffset, Vec2 endOffset, int textureId) {
        super(pos);

        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.textureId = textureId;

        this.doorDirection = startOffset.sub(endOffset).normalize();
    }

    public void update(double delta) {
        this.elapsedTime += delta;
    }

    public TileEntityRaycastResult castRay(Vec2 start, Vec2 dir) {
        Vec2 point1 = this.position.add(this.startOffset).add(this.doorDirection.mul((Math.cos(this.elapsedTime) - 1) / 2)); // Line start
        Vec2 point2 = this.position.add(this.endOffset).add(this.doorDirection.mul((Math.cos(this.elapsedTime) - 1) / 2)); // Line end

        Vec2 v1 = start.sub(point1);
        Vec2 v2 = point2.sub(point1);
        Vec2 v3 = new Vec2(-dir.y, dir.x);

        double dot = v2.dot(v3);
        if (Math.abs(dot) < 0.00001) {
            // No hit
            TileEntityRaycastResult res = new TileEntityRaycastResult();
            res.hit = false;

            return res;
        }

        double t1 = v2.cross(v1) / dot;
        double t2 = v1.dot(v3) / dot;

        if (t1 >= 0.0 && (t2 >= 0.0 && t2 <= 1.0)) {
            TileEntityRaycastResult res = new TileEntityRaycastResult();
            res.hit = true;
            res.distance = t1;
            res.precisePositition = start.add(dir.mul(t1));
            res.worldPositition = this.position.copy();
            res.entity = this;
            res.startOffset = res.precisePositition.sub(point1).len() / point1.sub(point2).len();

            return res;
        }

        TileEntityRaycastResult res = new TileEntityRaycastResult();
        res.hit = false;
        return res;
    }

    public int getTextureId() {
        return this.textureId;
    }

    private double elapsedTime = 0.0;
    private Vec2 startOffset;
    private Vec2 endOffset;
    private int textureId;

    private Vec2 doorDirection;
}