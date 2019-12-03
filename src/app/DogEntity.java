package app;
import java.util.List;

public class DogEntity extends MonsterEntity {
    DogEntity(Vec2 pos, Player player, List<AnimatedSprite> sprites) {
        super(pos, new Vec2(), new Vec2(), player);

        // Testing with only one now
        this.sprites = sprites;
    }

    @Override
    public void update(double delta, World world) {
        Vec2 pDir = this.position.sub(world.getPlayer().getPosition());
        Vec2 testDir = this.position.add(this.direction).sub(world.getPlayer().getPosition());
        Vec2 fDir = testDir.add(pDir).normalize().mul(-1);

        if ((fDir.x > 0 || fDir.x < 0) && Math.abs(fDir.x) > Math.abs(fDir.y)) {
            this.currentSpriteIndex = 1;
        } else if (fDir.y > 0) {
            this.currentSpriteIndex = 0;
        } else if (fDir.y < 0) {
            this.currentSpriteIndex = 2;
        }

        
        List<Vec2> path = world.getPathToPlayer(this.position);
        
        if (path.size() >= 2) {
            Vec2 dir = path.get(1).add(new Vec2(0.5, 0.5)).sub(this.position).normalize();
            //this.velocity = dir.mul(1);
        } else {
            //this.velocity = new Vec2();
        }

        if (this.velocity.len() > 0) {
            this.sprites.get(this.currentSpriteIndex).update(delta);
        }
    }

    @Override
    public Sprite getSprite() {
        return this.sprites.get(currentSpriteIndex).getSprite(this.position.copy(), true);
    }

    private List<AnimatedSprite> sprites;
    private int currentSpriteIndex;
}