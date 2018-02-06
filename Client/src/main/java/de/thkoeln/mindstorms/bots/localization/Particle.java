package de.thkoeln.mindstorms.bots.localization;

import de.thkoeln.mindstorms.bots.ui.Controller;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import lejos.robotics.geometry.Line;
import lejos.robotics.geometry.Point;

import java.util.List;
import java.util.Objects;

import static de.thkoeln.mindstorms.bots.ui.Controller.SCALE;

/**
 * Particle
 */
public class Particle {
    private double x, y, belief, angle;

    public Particle(double x, double y, double angle, double belief) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.belief = belief;
    }

    public synchronized void move(double distance) {
        x += Math.cos(Math.toRadians(angle)) * distance;
        y += Math.sin(Math.toRadians(angle)) * distance;
    }

    public synchronized void rotate(double degree) {
        angle += degree;
        angle %= 360;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getBelief() {
        return belief;
    }

    public void adjustBelief(double factor) {
        belief *= factor;
    }

    public double getAngle() {
        return angle;
    }

    public double measureDistance(List<Line> walls) {
        Point target = new Point((float) (x + Math.cos(Math.toRadians(angle)) * 250.0), (float) (y + Math.sin(Math.toRadians(angle)) * 250.0));
        final Line viewport = new Line((float) x, (float) y, target.x, target.y);
        return walls.stream()
                .map(line -> line.intersectsAt(viewport))
                .filter(Objects::nonNull)
                .mapToDouble(collision -> new Line((float) x, (float) y, collision.x, collision.y).length() * 10)
                .map(dist -> dist + 60)
                .min().orElse(0);
    }

    public void draw(GraphicsContext g) {
        g.setFill(Color.RED);
        g.setStroke(Color.RED);
        g.fillOval(SCALE * x - 1.5, SCALE * y - 1.5, 3, 3);
        g.setLineWidth(2);
        g.strokeLine(SCALE * x, SCALE * y, SCALE * x + Math.cos(Math.toRadians(angle)) * 1.5, SCALE * y + Math.sin(Math.toRadians(angle)) * 1.5);
        g.setLineWidth(1);
        g.strokeLine(SCALE * x, SCALE * y, SCALE * x + Math.cos(Math.toRadians(angle)) * 3.0, SCALE * y + Math.sin(Math.toRadians(angle)) * 3.0);
    }
}
