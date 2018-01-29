package de.thkoeln.mindstorms.bots;

import de.thkoeln.mindstorms.client.environment.properties.Disabled;
import de.thkoeln.mindstorms.client.environment.MindstormsBot;
import de.thkoeln.mindstorms.server.controlling.EV3Controller;

/**
 * DummyBot
 */
@Disabled
public class DummyBot extends MindstormsBot {

    public DummyBot(EV3Controller ctr) {
        super(ctr);
    }

    @Override
    public void run() throws Exception {
        for (int i = 0; i < 8; i++) {
            ctr.rotate(90).await();
        }

        System.exit(0);
    }

    public void read() {

    }
}
