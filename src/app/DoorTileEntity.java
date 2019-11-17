package app;

public class DoorTileEntity extends TileEntity {
    public DoorTileEntity(Vec2 pos, boolean lr) {
        super(pos);
    }

    public void update(double delta) {

    }

    public Vec2 getPoint1() {
        return this.position.add(new Vec2(0.5, 0.0)); // Line start
    }

    public Vec2 getPoint2() {
        return this.position.add(new Vec2(0.5, 1.0)); // Line start
    }

    public RayResult castRay(Vec2 start, Vec2 dir) {
        Vec2 point1 = this.getPoint1();
        Vec2 point2 = this.getPoint2();

        Vec2 v1 = start.sub(point1);
        Vec2 v2 = point2.sub(point1);
        Vec2 v3 = new Vec2(-dir.y, dir.x);

        double dot = v2.dot(v3);
        if (Math.abs(dot) < 0.00001) {
            // No hit
            RayResult res = new RayResult();
            res.hit = false;

            return res;
        }

        double t1 = v2.cross(v1) / dot;
        double t2 = v1.dot(v3) / dot;

        if (t1 >= 0.0 && (t2 >= 0.0 && t2 <= 1.0)) {
            RayResult res = new RayResult();
            res.hit = true;
            res.distance = t1;
            res.precisePositition = start.add(dir.mul(t1));
            res.worldPositition = this.position.copy();

            return res;
        }

        RayResult res = new RayResult();
        res.hit = false;
        return res;
    }
}