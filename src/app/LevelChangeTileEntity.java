package app;

public class LevelChangeTileEntity extends TileEntity {
    public LevelChangeTileEntity(Vec2 pos, String nextLevel, int textureId) {
        super(pos);

        this.textureId = textureId;
        this.nextLevel = nextLevel;
        this.change = false;
    }

    @Override
    public void update(double delta, World world) {
        if (this.change) {
            world.resetTo(LevelLoader.loadLevel(this.nextLevel, TextureRegistry.getInstance()));
        }
    }

    @Override
    public int getTextureId() {
        return this.textureId;
    }

    @Override
    public void onInteract() {
        this.change = true;
    }

    @Override
    public RaycastResult castRay(Vec2 start, Vec2 dir) {
        TileEntityRaycastResult res = new TileEntityRaycastResult(new Rect((int)this.position.x, (int)this.position.y, 1, 1).castRay(start.add(dir.mul(-0.001)), dir));
        res.entity = this;
        res.distance = 0;
        res.precisePositition = start;

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
    private String nextLevel;
    private boolean change;
}