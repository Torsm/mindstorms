package de.thkoeln.mindstorms.bots;

import de.thkoeln.mindstorms.client.environment.Disabled;
import de.thkoeln.mindstorms.client.environment.MindstormsBot;
import de.thkoeln.mindstorms.server.controlling.EV3Controller;

/**
 * DummyBot
 */
@Disabled
public class DummyBot extends MindstormsBot {
    private boolean travelling = true;

    public DummyBot(EV3Controller ctr) {
        super(ctr);
    }

    @Override
    public void run() throws Exception {
        ctr.rotateFrontDistanceSensorMotor(90);
        while (true) {
            System.out.println(ctr.readFrontDistanceSensor().get());
            Thread.sleep(1000);
        }
    }
}
