package app;

import java.util.List;

public class AnimatedSprite {
    public AnimatedSprite(List<Number> sprites, double cycleTime) {
        this.sprites = sprites;
        this.currentIndex = 0;
        this.elapsedTime = 0;
        this.cycleTime = cycleTime;
    }

    public void update(double delta) {
        this.elapsedTime += delta;

        if (this.elapsedTime >= this.cycleTime * this.sprites.size()) {
            this.elapsedTime -= this.cycleTime * this.sprites.size();
        }

        this.currentIndex = (int)(this.elapsedTime / this.cycleTime);
    }

    public void setElapsedTime(double time) {
        this.elapsedTime = time;
        this.currentIndex = (int)(this.elapsedTime / this.cycleTime);
    }

    public Sprite getSprite(Vec2 pos, boolean solid) {
        return new Sprite(pos, this.sprites.get(this.currentIndex).intValue(), solid);
    }

    public int getTextureId() {
        return this.sprites.get(this.currentIndex).intValue();
    }

    private List<Number> sprites;
    private int currentIndex;
    private double cycleTime;
    private double elapsedTime;
}