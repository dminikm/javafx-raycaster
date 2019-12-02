package app;

public class GameEndTileEntity extends TileEntity {
    public GameEndTileEntity(Vec2 pos, int textureId) {
        super(pos);

        this.textureId = textureId;
    }

    @Override
    public void update(double delta, World world) {}

    @Override
    public int getTextureId() {
        return this.textureId;
    }

    @Override
    public void onInteract() {
        // TODO: Implement end screen?
        System.exit(0);
    }

    @Override
    public TileEntityRaycastResult castRay(Vec2 start, Vec2 dir) {
        TileEntityRaycastResult res = new TileEntityRaycastResult(new Rect((int)this.position.x, (int)this.position.y, 1, 1).castRay(start.add(dir.mul(-0.001)), dir));
        res.entity = this;

        return res;
    }

    private int textureId;
}