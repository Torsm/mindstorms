package de.thkoeln.mindstorms.bots;

import de.thkoeln.mindstorms.client.MindstormsBot;
import de.thkoeln.mindstorms.server.controlling.EV3Controller;

/**
 * DummyBot
 */
@MindstormsBot
public class DummyBot implements Runnable {
    private final EV3Controller controller;

    public DummyBot(EV3Controller controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        controller.travel(30.0).onComplete(result ->
                controller.drawString("We just moved", 0, 0)
        );
    }
}
