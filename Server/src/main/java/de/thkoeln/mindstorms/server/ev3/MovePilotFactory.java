package de.thkoeln.mindstorms.server.ev3;

import lejos.hardware.motor.Motor;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;

/**
 * MovePilotFactory
 */
public class MovePilotFactory {

    public static MovePilot createMovePilot() {
        Wheel wheel1 = WheeledChassis.modelWheel(Motor.A, 50).offset(-72);
        Wheel wheel2 = WheeledChassis.modelWheel(Motor.B, 50).offset(72);
        Chassis chassis = new WheeledChassis(new Wheel[] { wheel1, wheel2 }, WheeledChassis.TYPE_DIFFERENTIAL);
        MovePilot movePilot = new MovePilot(chassis);
        movePilot.setLinearSpeed(150);
        movePilot.setLinearAcceleration(150);
        movePilot.setAngularAcceleration(100);
        return movePilot;
    }
}
