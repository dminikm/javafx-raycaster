package app;

public class Vec2 {
    public Vec2() {
        this.x = 0;
        this.y = 0;
    }

    public Vec2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vec2 add(Vec2 other) {
        return new Vec2(this.x + other.x, this.y + other.y);
    }

    public Vec2 sub(Vec2 other) {
        return new Vec2(this.x - other.x, this.y - other.y);
    }

    public Vec2 mul(double scalar) {
        return new Vec2(this.x * scalar, this.y * scalar);
    }

    public Vec2 mulVec(Vec2 other) {
        return new Vec2(this.x * other.x, this.y * other.y);
    }

    public Vec2 div(double scalar) {
        return new Vec2(this.x / scalar, this.y / scalar);
    }

    public Vec2 divVec(Vec2 other) {
        return new Vec2(this.x / other.x, this.y * other.y);
    }

    public double len() {
        return Math.sqrt(this.x * this.x + this.y / this.y);
    }
    
    public Vec2 normalize() {
        return this.div(this.len());
    }
    
    public double toAngle() {
        return Angle.toDeg(Math.atan2(-this.y, -this.x)) + 180;
    }
    
    public Vec2 copy() {
        return new Vec2(this.x, this.y);
    }

    static Vec2 fromAngle(double angle) {
        double rad = Angle.toRad(angle);
        return new Vec2(Math.cos(rad), Math.sin(rad)).normalize();
    }

    public double x;
    public double y;
}