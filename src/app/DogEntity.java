package app;
import java.util.List;

public class DogEntity extends MonsterEntity {
    DogEntity(Vec2 pos, Vec2 dir, Player player, List<AnimatedSprite> sprites) {
        super(pos, dir, new Vec2(), player);

        // Testing with only one now
        this.sprites = sprites;
    }

    @Override
    public void update(double delta, World world) {
        this.elapsedTime += delta;

        if (this.health <= 0) {
            this.currentSpriteIndex = this.sprites.size() - 1;
            this.sprites.get(this.currentSpriteIndex).update(delta);
            this.velocity = new Vec2();
            return;
        }

        Vec2 playerDir = world.getPlayer().getPosition().sub(this.position);
        double angle = Angle.normalizeDeg(playerDir.normalize().toAngle() - this.direction.toAngle());

        if (angle > 180 - 45 && angle < 180 + 45) {
            this.currentSpriteIndex = 3;    // Back
        } else if (angle > 360 - 45 || angle < 0 + 45) {
            this.currentSpriteIndex = 0;    // Front

            // IF player is in front of the dog, check if he is visible
            EntityRaycastResult res = world.castRayEntity(this.position, playerDir.normalize(), this);
            if (res.hit && res.entity instanceof Player) {
                // If so, alert this dog
                this.alerted = true;
            }

        } else if (angle > 90 - 45 && angle < 90 + 45) {
            this.currentSpriteIndex = 2;    // Right
        } else {
            this.currentSpriteIndex = 1;    // Left
        }

        if (this.alerted) {
            List<Vec2> path = world.getPathToPlayer(this.position);

            if (path.size() >= 2) {
                Vec2 dir = path.get(1).add(new Vec2(0.5, 0.5)).sub(this.position).normalize();
                this.direction = playerDir.normalize();
                this.velocity = dir.mul(1);
            } else {
                this.velocity = new Vec2();
            }

            // Update animation
            if (this.velocity.len() > 0) {
                this.sprites.get(this.currentSpriteIndex).update(delta);
            }

            // Check if player in range and the dog can bite again
            if (playerDir.len() < 2 && this.elapsedTime - this.attackDelay > this.lastBite) {
                this.fire(world);
                this.lastBite = this.elapsedTime;
            }
        }

        if (this.isFiring()) {
            this.currentSpriteIndex = this.sprites.size() - 2;
            this.sprites.get(this.currentSpriteIndex).update(delta);
        }
    }

    private void fire(World world) {
        Vec2 dir = world.getPlayer().getPosition().sub(this.position).normalize();
        EntityRaycastResult res = world.castRayEntity(this.position, dir, this);

        if (res.hit) {
            res.entity.takeDamage(this.damage);
        }

        // queue sound
        world.alertEntitiesInDistance(this.position, 10);
    }

    private boolean isFiring() {
        return this.elapsedTime - this.attackDelay < this.lastBite;
    }

    @Override
    public Sprite getSprite() {
        return this.sprites.get(currentSpriteIndex).getSprite(this.position.copy(), true);
    }

    private List<AnimatedSprite> sprites;
    private int currentSpriteIndex;

    private double elapsedTime = 0;
    private double lastBite = -10;

    private final double attackDelay = 1;
    private final int damage = 10;
}