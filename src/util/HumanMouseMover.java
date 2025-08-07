package util;

import org.dreambot.api.input.Mouse;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HumanMouseMover {
    private static final Random random = new Random();

    public static void moveMouse(Point target) {
        moveMouse(target.x, target.y);
    }

    public static void moveMouse(int targetX, int targetY) {
        Point start = Mouse.getPosition();
        Point target = new Point(targetX, targetY);

        Point control1 = getRandomControlPoint(start, target);
        Point control2 = getRandomControlPoint(target, start);

        List<Point> pathPoints = getBezierPoints(start, control1, control2, target, 30);

        for (Point point : pathPoints) {
            Mouse.move(point);
            sleep(randomBetween(5, 20)); // Delay between moves for natural speed
        }
    }

    private static Point getRandomControlPoint(Point p1, Point p2) {
        int dx = p2.x - p1.x;
        int dy = p2.y - p1.y;

        int offsetX = (int) (dx * 0.3) + randomBetween(-30, 30);
        int offsetY = (int) (dy * 0.3) + randomBetween(-30, 30);

        return new Point(p1.x + offsetX, p1.y + offsetY);
    }

    private static List<Point> getBezierPoints(Point start, Point c1, Point c2, Point end, int steps) {
        List<Point> points = new ArrayList<>();
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            points.add(getCubicBezierPoint(start, c1, c2, end, t));
        }
        return points;
    }

    private static Point getCubicBezierPoint(Point p0, Point p1, Point p2, Point p3, double t) {
        double u = 1 - t;
        double tt = t * t;
        double uu = u * u;
        double uuu = uu * u;
        double ttt = tt * t;

        double x = uuu * p0.x;
        x += 3 * uu * t * p1.x;
        x += 3 * u * tt * p2.x;
        x += ttt * p3.x;

        double y = uuu * p0.y;
        y += 3 * uu * t * p1.y;
        y += 3 * u * tt * p2.y;
        y += ttt * p3.y;

        return new Point((int) x, (int) y);
    }

    private static int randomBetween(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }
}
