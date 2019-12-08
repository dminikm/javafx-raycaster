package app;

public class TreasurePickupEntity extends PickupEntity {
  public TreasurePickupEntity(Vec2 pos, int textureId) {
    super(pos);
    this.textureId = textureId;
  }

  @Override
  protected void onPickup(Player p) {
    // Do nothing, treasure is useless
    this.delete = true;
  }
}
