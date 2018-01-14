package de.thkoeln.mindstorms.server.controlling;

import de.thkoeln.mindstorms.concurrency.ObservableRequest;
import de.thkoeln.mindstorms.server.ev3.MovePilotFactory;
import lejos.hardware.ev3.EV3;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.MeanFilter;
import lejos.robotics.navigation.MovePilot;

/**
 * EV3ServerController
 */
public class EV3ServerController implements EV3Controller {
    private final MovePilot movePilot;
    private final NXTRegulatedMotor frontDistanceSensorMotor;
    private final SensorMode backDistanceSensor;
    private final SensorMode frontDistanceSensor;
    private final SensorMode colorSensor;
    private double surfaceBias = 1.5;

    public EV3ServerController() {
        movePilot = MovePilotFactory.createMovePilot();
        EV3 ev3 = LocalEV3.get();

        frontDistanceSensorMotor = Motor.C;
        frontDistanceSensor = new EV3UltrasonicSensor(ev3.getPort("S4")).getMode("Distance");
        backDistanceSensor = new EV3UltrasonicSensor(ev3.getPort("S2")).getMode("Distance");
        colorSensor = new EV3ColorSensor(ev3.getPort("S3")).getColorIDMode();
    }

    @Override
    public ObservableRequest<Void> travel(double distance) {
        movePilot.travel(distance);
        return new ObservableRequest<>();
    }

    @Override
    public ObservableRequest<Void> rotate(double angle) {
        movePilot.rotate(angle / surfaceBias);
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
    public ObservableRequest<Float> readBackDistanceSensor() {
        MeanFilter meanFilter = new MeanFilter(backDistanceSensor, 5);
        float distance = readSamples(meanFilter)[0];
        return new ObservableRequest<>(distance);
    }

    @Override
    public ObservableRequest<Void> rotateFrontDistanceSensorMotor(int angle) {
        frontDistanceSensorMotor.rotate(angle);
        return new ObservableRequest<>();
    }

    private float[] readSamples(SampleProvider sensor) {
        float[] samples = new float[sensor.sampleSize()];
        sensor.fetchSample(samples, 0);
        return samples;
    }
}
