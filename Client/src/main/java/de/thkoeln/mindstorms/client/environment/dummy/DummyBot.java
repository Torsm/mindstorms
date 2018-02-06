package de.thkoeln.mindstorms.client.environment.dummy;

import de.thkoeln.mindstorms.bots.localization.Particle;
import javafx.scene.canvas.GraphicsContext;

/**
 * DummyBot
 */
public class DummyBot extends Particle {
    private int direction;
    private float sensorPosition;

    public DummyBot(double x, double y, double angle) {
        super(x, y, angle, 1);
    }

    @Override
    public void draw(GraphicsContext g) {

    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public float getSensorPosition() {
        return sensorPosition;
    }

    public void setSensorPosition(float sensorPosition) {
        this.sensorPosition = sensorPosition;
    }
}
