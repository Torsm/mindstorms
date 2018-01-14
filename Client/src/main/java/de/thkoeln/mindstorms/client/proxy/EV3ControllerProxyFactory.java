package de.thkoeln.mindstorms.client.proxy;

import de.thkoeln.mindstorms.client.MindstormsClient;
import de.thkoeln.mindstorms.server.controlling.EV3Controller;
import de.thkoeln.mindstorms.server.controlling.operation.Opcode;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;

/**
 * EV3ControllerProxyFactory
 */
public class EV3ControllerProxyFactory {

    public static EV3Controller createController(final MindstormsClient client) {
        return ((EV3Controller) Proxy.newProxyInstance(EV3Controller.class.getClassLoader(), new Class[]{EV3Controller.class},
                (proxy, method, args) -> {
                    if (method.isAnnotationPresent(Opcode.class))
                        return client.getDispatcherService().dispatch(method, args);
                    return null;
                }));
    }
}
