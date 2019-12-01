package app;

import java.util.HashMap;

public class TurretEntity extends MonsterEntity {
    TurretEntity(Vec2 pos, Player player, HashMap<String, Number> textures) {
        super(pos, new Vec2(), new Vec2(), player);

        this.idleTextureId = textures.get("idleTexture").intValue();
        this.firingTextureId1 = textures.get("firingTexture1").intValue();
        this.firingTextureId2 = textures.get("firingTexture2").intValue();
        this.firingTextureId3 = textures.get("firingTexture3").intValue();
        this.firingTextureId4 = textures.get("firingTexture4").intValue();


        this.currentTextureId = this.idleTextureId;
        this.elapsedTime = 0;
    }

    @Override
    public void update(double delta, World world) {
        this.elapsedTime += delta;

        if (!firing) {
            double dist = player.getPosition().sub(this.position).len();
            Vec2 dir = player.getPosition().sub(this.position).normalize();
            RaycastResult res = world.castRay(this.position, dir);

            if (dist < res.distance) {
                this.firing = true;
                this.firingTime = this.elapsedTime;
                this.target = this.player.getPosition().copy();
            }
        }

        double timeDiff = this.elapsedTime - this.firingTime;
        if (this.firing && timeDiff < 0.2) {
            this.currentTextureId = this.firingTextureId1;
        } else if (this.firing && timeDiff < 0.4) {
            this.currentTextureId = this.firingTextureId2;
        } else if (this.firing && timeDiff < 0.6) {
            this.currentTextureId = this.firingTextureId3;
        } else if (this.firing && timeDiff < 1) {
            this.currentTextureId = this.firingTextureId4;
        } else if (this.firing && timeDiff > 1) {
            this.currentTextureId = this.idleTextureId;
            this.firing = false;
            this.firingTime = 0;
            this.shotsFired = 0;
        }

        if (timeDiff > 0.2 * (this.shotsFired + 1) && timeDiff < 0.7) {
            this.shotsFired++;
            //fire
        }
    }

    @Override
    public Sprite getSprite() {
        Sprite spr = new Sprite();
        spr.pos = this.position.copy();
        spr.solid = false;
        spr.textureId = this.currentTextureId;

        return spr;
    }

    private int currentTextureId;

    // textures
    private int idleTextureId;
    private int firingTextureId1;
    private int firingTextureId2;
    private int firingTextureId3;
    private int firingTextureId4;

    private double elapsedTime;

    private double firingTime = 0;
    private boolean firing = false;
    private Vec2 target = new Vec2();
    private int shotsFired = 0;
}