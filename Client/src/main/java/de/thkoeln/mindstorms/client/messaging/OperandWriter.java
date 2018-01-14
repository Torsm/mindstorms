package de.thkoeln.mindstorms.client.messaging;

import de.thkoeln.mindstorms.server.messaging.ParameterType;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * OperandWriter
 */
public class OperandWriter {
    private final DataOutputStream dataOutputStream;

    public OperandWriter(DataOutputStream dataOutputStream) {
        this.dataOutputStream = dataOutputStream;
    }

    public void writeOperands(Method method, Object[] values) {
        Class<?>[] types = method.getParameterTypes();

        for (int i = 0; i < types.length; i++) {
            try {
                Objects.requireNonNull(ParameterType.getByType(types[i])).writeTo(dataOutputStream, values[i]);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(String.format("Failed to write operand at position: %d (%s)", i, types[i].getSimpleName()), e);
            } catch (NullPointerException e) {
                e.printStackTrace();
                throw new RuntimeException(String.format("Parameter type not supported: %s" , types[i].getCanonicalName()), e);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
