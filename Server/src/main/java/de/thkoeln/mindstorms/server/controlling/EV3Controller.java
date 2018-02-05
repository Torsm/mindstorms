package de.thkoeln.mindstorms.server.controlling;

import de.thkoeln.mindstorms.concurrency.ObservableRequest;
import de.thkoeln.mindstorms.server.controlling.operation.Opcode;

/**
 * EV3Controller
 */
public interface EV3Controller {
    @Opcode(0)
    ObservableRequest<Void> travel(double distance);

    @Opcode(1)
    ObservableRequest<Void> rotate(double angle);

    @Opcode(2)
    ObservableRequest<Void> setSurfaceBias(double surfaceBias);

    @Opcode(3)
    ObservableRequest<Void> setSpeed(double speed);

    @Opcode(4)
    ObservableRequest<Void> drawString(String string, int x, int y);

    @Opcode(5)
    ObservableRequest<Void> clearScreen();

    @Opcode(6)
    ObservableRequest<Float> readColorSensor();

    @Opcode(7)
    ObservableRequest<Float> readFrontDistanceSensor();

    @Opcode(8)
    ObservableRequest<Float> getSensorPosition();

    @Opcode(9)
    ObservableRequest<Void> rotateSensorMotor(int angle);

    @Opcode(10)
    ObservableRequest<Void> setLinearAcceleration(double acceleration);

    @Opcode(11)
    ObservableRequest<Void> setAngularAcceleration(double acceleration);

    @Opcode(12)
    ObservableRequest<Void> setDirection(int direction);

    @Opcode(13)
    ObservableRequest<Integer> getDirection();

    @Opcode(15)
    ObservableRequest<Integer> getMotorSpeed();

    @Opcode(16)
    ObservableRequest<Void> setMotorSpeed(int speed);

    @Opcode(17)
    ObservableRequest<Void> turnSensorTo(int angle);

    @Opcode(97)
    ObservableRequest<double[]> read3();

    @Opcode(18)
    ObservableRequest<float[]> getCurrentAngleData();
}
