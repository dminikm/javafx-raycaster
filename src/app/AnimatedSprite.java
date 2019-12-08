package app;

import java.util.List;

public class AnimatedSprite {
  public AnimatedSprite(List<Number> sprites, double cycleTime,
      boolean repeat) {
    this.sprites = sprites;
    this.elapsedTime = 0;
    this.cycleTime = cycleTime;
    this.repeat = repeat;
  }

  public void update(double delta) {
    this.elapsedTime += delta;

    if (this.elapsedTime >= this.cycleTime * this.sprites.size()
        && this.repeat) {
      this.elapsedTime -= this.cycleTime * this.sprites.size();
    }
  }

  public void setElapsedTime(double time) {
    this.elapsedTime = time;
  }

  public Sprite getSprite(Vec2 pos, boolean solid) {
    return new Sprite(pos, this.sprites.get(this.getCurrentIndex()).intValue(),
        solid);
  }

  public int getTextureId() {
    return this.sprites.get(this.getCurrentIndex()).intValue();
  }

  public int getCurrentIndex() {
    return Math.min((int) (this.elapsedTime / this.cycleTime),
        this.sprites.size() - 1);
  }

  private List<Number> sprites;
  private double cycleTime;
  private double elapsedTime;
  private boolean repeat;
}
