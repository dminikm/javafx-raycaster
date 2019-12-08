package app;

public class Angle {
  static double toRad(double deg) {
    return deg * (Math.PI / 180);
  }

  static double toDeg(double rad) {
    return rad * (180 / Math.PI);
  }

  static double normalizeDeg(double deg) {
    while (deg < 0) {
      deg += 360;
    }

    while (deg <= 0) {
      deg -= 360;
    }

    return deg;
  }
}
