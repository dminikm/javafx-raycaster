package app;

import java.util.List;

public class SecretDoorTileEntity extends TileEntity {
  public SecretDoorTileEntity(Vec2 pos, int textureId, List<Vec2> path) {
    super(pos);

    this.textureId = textureId;
    this.path = path;
    this.openingPosition = this.path.get(0).copy();
  }

  @Override
  public void update(double delta, World world) {
    if (this.opening) {
      this.openingTime = Math.min(((double) this.path.size() - 1) - 0.001,
          this.openingTime + delta);

      int pathNum = (int) (openingTime);
      double pathOffset = this.openingTime - pathNum;

      Vec2 dir =
          this.path.get(pathNum + 1).sub(this.path.get(pathNum)).normalize();
      this.openingPosition = this.path.get(pathNum).add(dir.mul(pathOffset));
    }
  }

  @Override
  public int getTextureId() {
    return this.textureId;
  }

  @Override
  public void onInteract() {
    if (!this.opening) {
      this.openingTime = 0;
      this.opening = true;
    }
  }

  @Override
  public RaycastResult castRay(Vec2 start, Vec2 dir) {
    if (this.opening
        && this.openingTime >= ((double) this.path.size() - 1) - 0.002) {
      return new TileEntityRaycastResult();
    }

    TileEntityRaycastResult res = new TileEntityRaycastResult(
        new Rect(this.openingPosition.x - 0.5, this.openingPosition.y - 0.5, 1,
            1).castRay(start.add(dir.mul(-0.001)), dir));
    res.entity = this;

    return res;
  }

  @Override
  public boolean isSolid() {
    return !(this.openingTime >= ((double) this.path.size() - 1) - 0.002);
  }

  @Override
  public boolean canApplyShading() {
    return true;
  }

  private int textureId;
  private List<Vec2> path;
  private boolean opening;
  private Vec2 openingPosition;
  private double openingTime;
}
