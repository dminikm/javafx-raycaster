package app;

public class DoorTileEntity extends TileEntity {
    public DoorTileEntity(Vec2 pos, Vec2 startOffset, Vec2 endOffset, int textureId) {
        super(pos);

        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.textureId = textureId;

        this.doorDirection = startOffset.sub(endOffset).normalize();
    }

    public void update(double delta, World world) {
        if (this.opening) {
            this.openedState = Math.min(1.0, this.openedState + delta);
        } else {
            this.openedState = Math.max(0.0, this.openedState - delta);
        }
    }

    public TileEntityRaycastResult castRay(Vec2 start, Vec2 dir) {
        Vec2 point1 = this.position.add(this.startOffset).add(this.doorDirection.mul(-this.openedState)); // Line start
        Vec2 point2 = this.position.add(this.endOffset).add(this.doorDirection.mul(-this.openedState)); // Line end

        TileEntityRaycastResult res = new TileEntityRaycastResult(new Line(point1, point2).castRay(start, dir));
        res.entity = this;

        return res;
    }

    public int getTextureId() {
        return this.textureId;
    }

    @Override
    public boolean isSolid() {
        return this.openedState < 0.2;
    }

    @Override
    public void onInteract() {
        this.opening = !this.opening;
    }

    private boolean opening = true;
    private double openedState = 0.0;

    private Vec2 startOffset;
    private Vec2 endOffset;
    private int textureId;

    private Vec2 doorDirection;
}