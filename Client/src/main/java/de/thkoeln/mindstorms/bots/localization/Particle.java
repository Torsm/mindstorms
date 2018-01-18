package de.thkoeln.mindstorms.bots.localization;

import java.awt.*;

/**
 * Particle
 */
public class Particle {
    private double x, y, believe, angle;

    public Particle(double x, double y, double believe) {
        this.x = x;
        this.y = y;
        this.believe = believe;
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

    public double getBelieve() {
        return believe;
    }

    public void adjustBelieve(double factor) {
        believe *= factor;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }
}
