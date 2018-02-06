package de.thkoeln.mindstorms;

import de.thkoeln.mindstorms.client.environment.BotLoader;
import de.thkoeln.mindstorms.client.environment.dummy.EV3DummyController;
import de.thkoeln.mindstorms.server.controlling.EV3Controller;
import org.junit.Test;

/**
 * Simulation
 */
public class Simulation {

    @Test
    public void startSimulation() {
        EV3Controller controller = new EV3DummyController();
        BotLoader.executeBots(controller);
    }
}
