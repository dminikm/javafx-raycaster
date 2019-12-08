package app;

public class HealthPickupEntity extends PickupEntity {
  public HealthPickupEntity(Vec2 pos, int textureId, int amount) {
    super(pos);
    this.amount = amount;

    this.textureId = textureId;
    this.solid = false;
  }

  @Override
  protected void onPickup(Player p) {
    this.delete = true;
    p.takeDamage(-this.amount);
  }

  private int amount;
}
