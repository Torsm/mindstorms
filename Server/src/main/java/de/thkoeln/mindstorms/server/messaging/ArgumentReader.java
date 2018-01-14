package de.thkoeln.mindstorms.server.messaging;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * ArgumentReader
 */
public class ArgumentReader {
    private final DataInputStream dataInputStream;

    public ArgumentReader(DataInputStream dataInputStream) {
        this.dataInputStream = dataInputStream;
    }

    public Object[] readOperands(Method method) {
        Class<?>[] types = method.getParameterTypes();
        Object[] operands = new Object[types.length];

        for (int i = 0; i < types.length; i++) {
            try {
                operands[i] = Objects.requireNonNull(ParameterType.getByType(types[i])).readFrom(dataInputStream);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(String.format("Failed to read operand at position: %d (%s)", i, types[i].getSimpleName()), e);
            } catch (NullPointerException e) {
                e.printStackTrace();
                throw new RuntimeException(String.format("Parameter type not supported: %s" , types[i].getCanonicalName()), e);
            }
        }

        return operands;
    }
}
