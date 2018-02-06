package de.thkoeln.mindstorms.client.environment.dummy;

import com.sun.deploy.security.BlacklistedCerts;
import de.thkoeln.mindstorms.bots.localization.Particle;
import de.thkoeln.mindstorms.bots.ui.Controller;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import lejos.robotics.geometry.Line;

import java.util.List;

import static de.thkoeln.mindstorms.bots.ui.Controller.SCALE;

/**
 * DummyBot
 */
public class DummyBot extends Particle {
    private final List<Line> lines;
    private int direction;
    private float sensorPosition;

    public DummyBot(double x, double y, double angle) {
        super(x, y, angle, 1);
        lines = Controller.loadMap("/image/room.svg");
    }

    @Override
    public void draw(GraphicsContext g) {
        g.setFill(Color.rgb(0, 255, 0, 0.5));
        g.setStroke(Color.BLACK);
        g.fillOval(SCALE * getX() - SCALE * 6, SCALE * getY() - SCALE * 6, SCALE * 12, SCALE * 12);
        g.setLineWidth(2);
        g.strokeLine(SCALE * getX(), SCALE * getY(), SCALE * getX() + Math.cos(Math.toRadians(getAngle())) * SCALE * 6, SCALE * getY() + Math.sin(Math.toRadians(getAngle())) * SCALE * 6);

        g.setStroke(Color.BLUE);
        Particle sensor = new Particle(getX(), getY(), getAngle(), 0);
        sensor.move(6);
        sensor.rotate(sensorPosition);
        g.strokeLine(SCALE * sensor.getX(), SCALE * sensor.getY(), SCALE * sensor.getX() + Math.cos(Math.toRadians(sensor.getAngle())) * SCALE * 3, SCALE * sensor.getY() + Math.sin(Math.toRadians(sensor.getAngle())) * SCALE * 3);
    }

    @Override
    public double measureDistance(List<Line> walls) {
        Particle sensor = new Particle(getX(), getY(), getAngle(), 0);
        sensor.move(6);
        sensor.rotate(sensorPosition);
        return sensor.measureDistance(lines);
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
