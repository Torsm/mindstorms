package de.thkoeln.mindstorms.client.environment.dummy;

import de.thkoeln.mindstorms.concurrency.ObservableRequest;
import de.thkoeln.mindstorms.server.controlling.EV3Controller;

import java.util.concurrent.ThreadLocalRandom;

/**
 * EV3DummyController
 */
public class EV3DummyController implements EV3Controller {
    private final DummyBot bot;

    public EV3DummyController() {
        boolean b = ThreadLocalRandom.current().nextDouble() < 2.0 / 11.0;

        float x = (float) ThreadLocalRandom.current().nextDouble(0, b ? 100 : 150);
        float y = (float) ThreadLocalRandom.current().nextDouble(b ? 150 : 0, b ? 200 : 150);

        double angle = ThreadLocalRandom.current().nextDouble(0, 360);
        bot = new DummyBot(x, y, angle);
    }

    public DummyBot getBot() {
        return bot;
    }

    @Override
    public ObservableRequest<Void> travel(double distance) {
        bot.move(distance / 10.0); // cm
        return new ObservableRequest<>();
    }

    @Override
    public ObservableRequest<Void> rotate(double angle) {
        bot.rotate(angle);
        return new ObservableRequest<>();
    }

    @Override
    public ObservableRequest<Void> setSurfaceBias(double surfaceBias) {
        return new ObservableRequest<>();
    }

    @Override
    public ObservableRequest<Void> setSpeed(double speed) {
        return new ObservableRequest<>();
    }

    @Override
    public ObservableRequest<Void> drawString(String string, int x, int y) {
        System.out.println(string);
        return new ObservableRequest<>();
    }

    @Override
    public ObservableRequest<Void> clearScreen() {
        return new ObservableRequest<>();
    }

    @Override
    public ObservableRequest<Float> readColorSensor() {
        return new ObservableRequest<>(0f);
    }

    @Override
    public ObservableRequest<Float> readFrontDistanceSensor() {
        return new ObservableRequest<>((float) (bot.measureDistance(null) / 1000.0)); // m
    }

    @Override
    public ObservableRequest<Float> getSensorPosition() {
        return new ObservableRequest<>(bot.getSensorPosition());
    }

    @Override
    public ObservableRequest<Void> rotateSensorMotor(int angle) {
        bot.setSensorPosition(bot.getSensorPosition() + angle);
        return new ObservableRequest<>();
    }

    @Override
    public ObservableRequest<Void> setLinearAcceleration(double acceleration) {
        return new ObservableRequest<>();
    }

    @Override
    public ObservableRequest<Void> setAngularAcceleration(double acceleration) {
        return new ObservableRequest<>();
    }

    @Override
    public ObservableRequest<Void> setDirection(int direction) {
        bot.setDirection(direction);
        return new ObservableRequest<>();
    }

    @Override
    public ObservableRequest<Integer> getDirection() {
        return new ObservableRequest<>(bot.getDirection());
    }

    @Override
    public ObservableRequest<Integer> getMotorSpeed() {
        return new ObservableRequest<>(1);
    }

    @Override
    public ObservableRequest<Void> setMotorSpeed(int speed) {
        return new ObservableRequest<>();
    }

    @Override
    public ObservableRequest<Void> turnSensorTo(int angle) {
        rotateSensorMotor(angle - (int) bot.getSensorPosition());
        return new ObservableRequest<>();
    }

    @Override
    public ObservableRequest<double[]> read3() {
        return new ObservableRequest<>(new double[]{readFrontDistanceSensor().await(), readFrontDistanceSensor().await(), readFrontDistanceSensor().await()});
    }

    @Override
    public ObservableRequest<float[]> getCurrentAngleData() {
        float[] results = {getSensorPosition().await(), readFrontDistanceSensor().await()};
        return new ObservableRequest<>(results);
    }
}
