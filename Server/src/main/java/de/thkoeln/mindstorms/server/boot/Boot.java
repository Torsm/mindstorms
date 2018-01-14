package de.thkoeln.mindstorms.server.boot;

import de.thkoeln.mindstorms.server.MindstormsServer;
import lejos.hardware.lcd.LCD;
import lejos.robotics.navigation.MovePilot;

import java.io.IOException;
import java.util.Scanner;

public class Boot {
	
	public static void main(String... args) {
		MindstormsServer.run(MindstormsServer.PORT);
	}
}
