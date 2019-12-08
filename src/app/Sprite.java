package app;

public class Sprite {
  public Sprite(Vec2 pos, int textureId, boolean solid) {
    this.pos = pos;
    this.textureId = textureId;
    this.solid = solid;
  }

  public Vec2 pos;
  public int textureId;
  public boolean solid;
}
