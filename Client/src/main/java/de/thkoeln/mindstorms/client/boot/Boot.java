package de.thkoeln.mindstorms.client.boot;

import de.thkoeln.mindstorms.client.MindstormsClient;
import de.thkoeln.mindstorms.client.environment.BotLoader;
import de.thkoeln.mindstorms.client.proxy.EV3ControllerProxyFactory;
import de.thkoeln.mindstorms.server.MindstormsServer;
import de.thkoeln.mindstorms.server.controlling.EV3Controller;

/**
 * Boot
 */
public class Boot {

    public static void main(String... args) {
        final MindstormsClient client = MindstormsClient.connect(args[0], MindstormsServer.PORT);
        final EV3Controller controller = EV3ControllerProxyFactory.createController(client);

        BotLoader.executeBots(controller);

        client.exit();
    }
}
