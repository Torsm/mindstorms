package de.thkoeln.mindstorms.server.controlling;

import de.thkoeln.mindstorms.concurrency.ObservableRequest;
import de.thkoeln.mindstorms.server.controlling.operation.Opcode;
import de.thkoeln.mindstorms.server.ev3.MovePilotFactory;
import lejos.hardware.ev3.EV3;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.MeanFilter;
import lejos.robotics.navigation.MovePilot;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * EV3ServerController
 */
public class EV3ServerController implements EV3Controller {
    private final MovePilot movePilot;
    private final NXTRegulatedMotor frontDistanceSensorMotor;
    private final SensorMode frontDistanceSensor;
    private final SensorMode colorSensor;
    private double surfaceBias = 0.7;
    private int direction = 1;

    public EV3ServerController() {
        movePilot = MovePilotFactory.createMovePilot();
        EV3 ev3 = LocalEV3.get();

        frontDistanceSensorMotor = Motor.C;
        frontDistanceSensor = new EV3UltrasonicSensor(ev3.getPort("S2")).getMode("Distance");
        colorSensor = new EV3ColorSensor(ev3.getPort("S3")).getRGBMode();
    }

    @Override
    public ObservableRequest<Void> travel(double distance) {
        movePilot.travel(distance);
        return new ObservableRequest<>();
    }

    @Override
    public ObservableRequest<Void> rotate(double angle) {
        movePilot.rotate(angle * surfaceBias);
        return new ObservableRequest<>();
    }

    @Override
    public ObservableRequest<Void> setSurfaceBias(double surfaceBias) {
        this.surfaceBias = surfaceBias;
        return new ObservableRequest<>();
    }

    @Override
    public ObservableRequest<Void> setSpeed(double speed) {
        movePilot.setLinearSpeed(speed);
        return new ObservableRequest<>();
    }

    @Override
    public ObservableRequest<Void> drawString(String string, int x, int y) {
        LCD.drawString(string, x, y);
        return new ObservableRequest<>();
    }

    @Override
    public ObservableRequest<Void> clearScreen() {
        LCD.clear();
        return new ObservableRequest<>();
    }

    @Override
    public ObservableRequest<Float> readColorSensor() {
        float color = readSamples(colorSensor)[0];
        return new ObservableRequest<>(color);
    }

    @Override
    public ObservableRequest<Float> readFrontDistanceSensor() {
        MeanFilter meanFilter = new MeanFilter(frontDistanceSensor, 5);
        float distance = readSamples(meanFilter)[0];
        return new ObservableRequest<>(distance);
    }

    @Override
    public ObservableRequest<Void> rotateSensorMotor(int angle) {
        frontDistanceSensorMotor.rotate(angle);
        return new ObservableRequest<>();
    }

    @Override
    public ObservableRequest<Float> getSensorPosition() {
        return new ObservableRequest<>(frontDistanceSensorMotor.getPosition());
    }

    @Override
    public ObservableRequest<Void> setLinearAcceleration(double acceleration) {
        movePilot.setLinearAcceleration(acceleration);
        return new ObservableRequest<>();
    }

    @Override
    public ObservableRequest<Void> setAngularAcceleration(double acceleration) {
        movePilot.setAngularAcceleration(acceleration);
        return new ObservableRequest<>();
    }

    @Override
    public ObservableRequest<Void> setDirection(int direction) {
        this.direction = direction;
        return new ObservableRequest<>();
    }

    @Override
    public ObservableRequest<Integer> getDirection() {
        return new ObservableRequest<>(direction);
    }

    @Override
    public ObservableRequest<Integer> getMotorSpeed() {
        return new ObservableRequest<>(frontDistanceSensorMotor.getSpeed());
    }

    @Override
    public ObservableRequest<Void> setMotorSpeed(int speed) {
        frontDistanceSensorMotor.setSpeed(speed);
        return new ObservableRequest<>();
    }

    @Override
    public ObservableRequest<Void> turnSensorTo(int angle) {
        frontDistanceSensorMotor.rotate(angle - (int) frontDistanceSensorMotor.getPosition());
        return new ObservableRequest<>();
    }

    @Override
    public ObservableRequest<double[]> read3() {
        return new ObservableRequest<>(new double[]{readFrontDistanceSensor().await(), readFrontDistanceSensor().await(), readFrontDistanceSensor().await()});
    }

    @Override
    public ObservableRequest<float[]> getCurrentAngleData() {
        MeanFilter meanFilter = new MeanFilter(frontDistanceSensor, 5);
        float distance = readSamples(meanFilter)[0];
        float position = frontDistanceSensorMotor.getPosition();

        float[] results = {position, distance};
        return new ObservableRequest<>(results);
    }

    private float[] readSamples(SampleProvider sensor) {
        float[] samples = new float[sensor.sampleSize()];
        sensor.fetchSample(samples, 0);
        return samples;
    }
}
