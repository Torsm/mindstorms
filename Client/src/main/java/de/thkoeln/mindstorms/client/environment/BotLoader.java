package de.thkoeln.mindstorms.client.environment;

import de.thkoeln.mindstorms.server.controlling.EV3Controller;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;

/**
 * BotLoader
 */
public class BotLoader {

    public static void executeBots(final EV3Controller controller) {
        new Reflections("de.thkoeln.mindstorms.bots").getSubTypesOf(MindstormsBot.class).stream()
                .filter(type -> !type.isAnnotationPresent(Disabled.class))
                .sorted(Comparator.comparingInt(type -> type.isAnnotationPresent(Priority.class) ? type.getAnnotation(Priority.class).value() : 0))
                .forEach(type -> {
                    try {
                        System.out.println("Starting bot: " + type.getSimpleName());
                        Object instance = type.getDeclaredConstructor(EV3Controller.class).newInstance(controller);
                        try {
                            ((MindstormsBot) instance).run();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                });
    }
}
