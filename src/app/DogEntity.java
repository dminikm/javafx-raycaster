package app;

import java.util.List;

import javafx.scene.media.AudioClip;

public class DogEntity extends MonsterEntity {
  DogEntity(Vec2 pos, Vec2 dir, List<AnimatedSprite> sprites,
      AudioClip hurtSound, AudioClip attackSound) {
    super(pos, dir, new Vec2());

    // Testing with only one now
    this.sprites = sprites;

    this.hurtSound = hurtSound;
    this.attackSound = attackSound;

    this.health = 40;
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

    this.currentSpriteIndex = this.getAngleToPlayer(world);
    if (this.currentSpriteIndex == 0) {
      // IF player is in front of the soldier, check if he is visible
      EntityRaycastResult res =
          world.castRayEntity(this.position, playerDir.normalize(), this);
      if (res.hit && res.entity instanceof Player) {
        // If so, alert this soldier
        this.alerted = true;
      }
    }

    if (this.alerted) {
      // Get path to player
      List<Vec2> path = world.getPathToPlayer(this.position);

      if (path.size() >= 2) {
        // Move along the path
        Vec2 dir =
            path.get(1).add(new Vec2(0.5, 0.5)).sub(this.position).normalize();
        this.direction = dir.normalize();
        this.velocity = dir.mul(1);
      } else {
        this.velocity = new Vec2();
      }

      // Update animation
      if (this.velocity.len() > 0) {
        this.sprites.get(this.currentSpriteIndex).update(delta);
      }

      // Check if player in range and the dog can bite again
      if (playerDir.len() < 2
          && this.elapsedTime - this.attackDelay > this.lastBite) {
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
    this.attackSound.play();

    world.alertEntitiesInDistance(this.position, 10);
  }

  private boolean isFiring() {
    return this.elapsedTime - this.attackDelay < this.lastBite;
  }

  @Override
  public Sprite getSprite() {
    return this.sprites.get(currentSpriteIndex).getSprite(this.position.copy(),
        this.health > 0);
  }

  @Override
  public void takeDamage(double damage) {
    super.takeDamage(damage);

    if (!this.hurtSound.isPlaying()) {
      this.hurtSound.play();
    }

    this.alerted = true;
  }

  private List<AnimatedSprite> sprites;
  private int currentSpriteIndex;

  private double elapsedTime = 0;
  private double lastBite = -10;

  private final double attackDelay = 1;
  private final int damage = 10;

  private AudioClip hurtSound;
  private AudioClip attackSound;
}
