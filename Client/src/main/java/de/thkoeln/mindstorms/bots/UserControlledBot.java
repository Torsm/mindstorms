package de.thkoeln.mindstorms.bots;

import de.thkoeln.mindstorms.bots.ui.BotApplication;
import de.thkoeln.mindstorms.client.environment.MindstormsBot;
import de.thkoeln.mindstorms.client.environment.properties.Disabled;
import de.thkoeln.mindstorms.server.controlling.EV3Controller;
import javafx.application.Application;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * UserControlledBot
 */
public class UserControlledBot extends MindstormsBot {
    private final static AtomicInteger COUNTER = new AtomicInteger(0);
    private final static Map<Integer, EV3Controller> CONTROLLERS = new ConcurrentHashMap<>();
    private final int id;

    public UserControlledBot(EV3Controller ctr) {
        super(ctr);
        id = COUNTER.getAndIncrement();
        CONTROLLERS.put(id, ctr);
    }

    @Override
    public void run() {
        Application.launch(BotApplication.class, String.valueOf(id));
    }

    public static EV3Controller getController(int id) {
        return CONTROLLERS.get(id);
    }
}
