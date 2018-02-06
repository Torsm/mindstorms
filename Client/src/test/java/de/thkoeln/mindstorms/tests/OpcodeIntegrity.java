package de.thkoeln.mindstorms.tests;

import de.thkoeln.mindstorms.server.controlling.EV3Controller;
import de.thkoeln.mindstorms.server.controlling.operation.Opcode;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * OpcodeIntegrity
 */
public class OpcodeIntegrity {

    @Test
    public void opcodeIntegrityTest() {
        Map<Byte, Long> opcodeFrequency = Arrays.stream(EV3Controller.class.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Opcode.class))
                .map(method -> method.getAnnotation(Opcode.class).value())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Assert.assertTrue(opcodeFrequency.values().stream().allMatch(l -> l == 1));
    }
}
