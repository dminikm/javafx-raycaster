package app;

public class AmmoPickupEntity extends PickupEntity {
    public AmmoPickupEntity(Vec2 pos, int textureId, String weaponName, int amount, boolean makeAvailable) {
        super(pos);
        this.amount = amount;
        this.makeAvailable = makeAvailable;
        this.weaponName = weaponName;

        this.textureId = textureId;
        this.solid = false;
    }

    @Override
    protected void onPickup(Player p) {
        this.delete = true;

        Weapon w = p.getWeaponByName(this.weaponName);
        if (w != null) {
            if (this.makeAvailable) {
                w.setAvailable(true);
                p.setWeapon(w);
            }

            w.addAmmo(amount);
        }
    }

    private int amount;
    private String weaponName;
    private boolean makeAvailable;
}