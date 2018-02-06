package de.thkoeln.mindstorms.bots.localization;

import de.thkoeln.mindstorms.bots.ui.Controller;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import lejos.robotics.geometry.Line;

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

    public double getX() {
        return x;
    }

    public synchronized void move(double distance) {
        x += Math.cos(Math.toRadians(angle)) * distance;
        y += Math.sin(Math.toRadians(angle)) * distance;
    }

    public synchronized void rotate(double degree) {
        angle += degree;
        angle %= 360;
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

    public void setBelief(double belief) {
        this.belief = belief;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public boolean isRelevant(Line line) {
        return line.y1 == line.y2 ? isBetween(line.x1, line.x2, x) : isBetween(line.y1, line.y2, y);
    }
    private boolean isBetween(double a, double b, double c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
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
