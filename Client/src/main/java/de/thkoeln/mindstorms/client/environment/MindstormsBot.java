package de.thkoeln.mindstorms.client.environment;

import de.thkoeln.mindstorms.server.controlling.EV3Controller;

/**
 * MindstormsBot
 */
public abstract class MindstormsBot {
    protected final EV3Controller ctr;

    protected MindstormsBot(EV3Controller ctr) {
        this.ctr = ctr;
    }

    public abstract void run() throws Exception;
}
