package de.thkoeln.mindstorms.bots;

import de.thkoeln.mindstorms.client.environment.MindstormsBot;
import de.thkoeln.mindstorms.client.environment.properties.Disabled;
import de.thkoeln.mindstorms.server.controlling.EV3Controller;

import java.awt.geom.Line2D;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 * DummyBot
 */
@Disabled
public class DummyBot extends MindstormsBot {
    private boolean rotating;

    public DummyBot(EV3Controller ctr) {
        super(ctr);
    }

    @Override
    public void run() throws Exception {
        ctr.setMotorSpeed(100).await();
        ctr.turnSensorTo(0);
    }
}
