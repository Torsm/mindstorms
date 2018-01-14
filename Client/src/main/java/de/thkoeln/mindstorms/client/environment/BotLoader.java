package de.thkoeln.mindstorms.client.environment;

import de.thkoeln.mindstorms.client.MindstormsBot;
import de.thkoeln.mindstorms.server.controlling.EV3Controller;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.List;

/**
 * BotLoader
 */
public class BotLoader {

    public static void executeBots(final EV3Controller controller) {
        new Reflections("de.thkoeln.mindstorms.bots").getTypesAnnotatedWith(MindstormsBot.class).stream()
                .filter(type -> type.getAnnotation(MindstormsBot.class).enabled())
                .sorted(Comparator.comparingInt(type -> type.getAnnotation(MindstormsBot.class).priority()))
                .forEach(type -> {
                    try {
                        System.out.println("Starting bot: " + type.getSimpleName());
                        Object instance = type.getDeclaredConstructor(EV3Controller.class).newInstance(controller);
                        if (instance instanceof Runnable)
                            ((Runnable) instance).run();
                    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                });
    }
}
