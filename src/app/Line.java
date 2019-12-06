package app;

public class Line implements RayCastable {
    Line(Vec2 start, Vec2 end) {
        this.start = start;
        this.end = end;
    }

    public RaycastResult castRay(Vec2 start, Vec2 dir) {
        Vec2 v1 = start.sub(this.start);
        Vec2 v2 = this.end.sub(this.start);
        Vec2 v3 = new Vec2(-dir.y, dir.x);

        double dot = v2.dot(v3);
        if (Math.abs(dot) < 0.00001) {
            // No hit
            RaycastResult res = new TileEntityRaycastResult();
            res.hit = false;

            return res;
        }

        double t1 = v2.cross(v1) / dot;
        double t2 = v1.dot(v3) / dot;

        if (t1 >= 0.0 && (t2 >= 0.0 && t2 <= 1.0)) {
            RaycastResult res = new RaycastResult();
            res.hit = true;
            res.distance = t1;
            res.precisePosition = start.add(dir.mul(t1));
            res.worldPositition = this.start.copy();
            res.startOffset = res.precisePosition.distance(this.start) / this.start.distance(this.end);//res.precisePositition.sub(this.start).len() / this.start.sub(this.end).len();

            return res;
        }

        RaycastResult res = new TileEntityRaycastResult();
        res.hit = false;
        return res;
    }

    public Vec2 start;
    public Vec2 end;
}