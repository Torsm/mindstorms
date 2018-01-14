package de.thkoeln.mindstorms.server.messaging;

import de.thkoeln.mindstorms.server.controlling.operation.OpcodeResolver;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * RequestReceiverService
 */
public class RequestReceiverService implements Runnable {
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final DataInputStream dataInputStream;
    private final ArgumentReader argumentReader;
    private final ResponderService responderService;
    private final OpcodeResolver opcodeResolver;

    public RequestReceiverService(InputStream in, ResponderService responderService, OpcodeResolver opcodeResolver) {
        this.dataInputStream = new DataInputStream(in);
        this.argumentReader = new ArgumentReader(this.dataInputStream);
        this.responderService = responderService;
        this.opcodeResolver = opcodeResolver;
    }

    @Override
    public void run() {
        try {
            int id = dataInputStream.readInt();
            byte opcode = dataInputStream.readByte();

            Method method = opcodeResolver.resolve(opcode);
            Object[] parameters = argumentReader.readOperands(method);

            responderService.submit(id, method, parameters);
        } catch (IOException e) {
            shutdown();
        }
    }

    public void start() {
        executorService.scheduleWithFixedDelay(this, 1, 1, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
