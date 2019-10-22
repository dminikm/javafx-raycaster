package app;

public class Angle {
    static double toRad(double deg) {
        return deg * (Math.PI / 180);
    }
    
    static double toDeg(double rad) {
        return rad * (180 / Math.PI);
    }
}