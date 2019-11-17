package app;

public class Rect {
    public Rect() {
        this.x = 0;
        this.y = 0;
        this.w = 0;
        this.h = 0;
    }

    public Rect(double x, double y, double w, double h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public boolean collidesWith(Rect other) {
        return (
            this.x  < other.x + other.w  &&
            other.x < this.x  + this.w   &&
            this.y  < other.y + other.h  &&
            other.y < this.y  + this.h
        );
    }

    public Vec2 getOverlap(Rect other) {
        Vec2 overlap = new Vec2();
        
        if (this.x < other.x) {
            overlap.x = -(other.x - (this.x + this.w));
        } else {
            overlap.x = this.x - (other.x + other.w);
        }

        if (this.y < other.y) {
            overlap.y = -(other.y - (this.y + this.h));
        } else {
            overlap.y = this.y - (other.y + other.h);
        }

        return overlap;
    }

    public Rect move(Vec2 other) {
        return new Rect(this.x + other.x, this.y + other.y, this.w, this.h);
    }

    public Rect copy() {
        return new Rect(this.x, this.y, this.w, this.h);
    }

    public double x;
    public double y;
    public double w;
    public double h;
}