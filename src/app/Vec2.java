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

    public Vec2 div(double scalar) {
        return new Vec2(this.x / scalar, this.y / scalar);
    }

    public double len() {
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }

    public double distance(Vec2 other) {
        return this.sub(other).len();
    }
    
    public Vec2 normalize() {
        return this.div(this.len());
    }
    
    public double dot(Vec2 other) {
        return (this.x * other.x) + (this.y * other.y);
    }

    public double cross(Vec2 other) {
        return (this.x * other.y) - (this.y * other.x);
    }

    public double toAngle() {
        return Angle.toDeg(Math.atan2(-this.y, -this.x)) + 180;
    }
    
    public Vec2 copy() {
        return new Vec2(this.x, this.y);
    }

    public Vec2 floor() {
        return new Vec2((int)this.x, (int)this.y);
    }

    static Vec2 fromAngle(double angle) {
        double rad = Angle.toRad(angle);
        return new Vec2(Math.cos(rad), Math.sin(rad)).normalize();
    }

    public boolean eq(Vec2 other) {
        return this.x == other.x && this.y == other.y;
    }

    public boolean eqi(Vec2 other) {
        return (int)this.x == (int)other.x && (int)this.y == (int)other.y;
    }

    public double x;
    public double y;
}