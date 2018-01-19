package de.thkoeln.mindstorms.bots.localization;

import java.awt.*;

/**
 * Particle
 */
public class Particle {
    private double x, y, belief, angle;

    public Particle(double x, double y, double belief) {
        this.x = x;
        this.y = y;
        this.belief = belief;
    }

    public double getX() {
        return x;
    }

    public void adjustX(double offset) {
        x += offset;
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
}
