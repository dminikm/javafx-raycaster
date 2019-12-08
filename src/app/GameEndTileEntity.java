package app;

public class GameEndTileEntity extends TileEntity {
    public GameEndTileEntity(Vec2 pos, int textureId) {
        super(pos);

        this.textureId = textureId;
        this.gameEnd = false;
    }

    @Override
    public void update(double delta, World world) {
        if (this.gameEnd) {
            world.endGame();
        }
    }

    @Override
    public int getTextureId() {
        return this.textureId;
    }

    @Override
    public void onInteract() {
        this.gameEnd = true;
    }

    @Override
    public RaycastResult castRay(Vec2 start, Vec2 dir) {
        TileEntityRaycastResult res = new TileEntityRaycastResult(new Rect((int)this.position.x, (int)this.position.y, 1, 1).castRay(start.add(dir.mul(-0.001)), dir));
        res.entity = this;
        res.precisePosition = start;
        res.distance = 0;

        return res;
    }

    @Override
    public boolean isSolid() {
        return true;
    }

    @Override
    public boolean canApplyShading() {
        return true;
    }

    private int textureId;
    private boolean gameEnd;
}