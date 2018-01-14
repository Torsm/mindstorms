package de.thkoeln.mindstorms.server.controlling.operation;

import de.thkoeln.mindstorms.server.controlling.EV3Controller;
import de.thkoeln.mindstorms.server.controlling.EV3ServerController;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * OpcodeResolver
 */
public class OpcodeResolver {
    private final Map<Byte, Method> operationTable = new HashMap<>();
    private Class<?> interfaceType;

    public OpcodeResolver(EV3Controller controller) {
        for (Class<?> interfaceType :controller.getClass().getInterfaces()){
            if (interfaceType.equals(EV3Controller.class)) {
                this.interfaceType = interfaceType;
            }
        }
    }

    public Method resolve(byte opcode) {
        Method method = operationTable.get(opcode);
        if (method == null) {
            for (Method declaredMethod : interfaceType.getDeclaredMethods()) {
                if (declaredMethod.isAnnotationPresent(Opcode.class) && declaredMethod.getAnnotation(Opcode.class).value() == opcode) {
                    operationTable.put(opcode, declaredMethod);
                    return declaredMethod;
                }
            }
        }

        if (method == null)
            throw new RuntimeException(String.format("Method with opcode %d not found", opcode), new NoSuchMethodException());

        return method;
    }
}
