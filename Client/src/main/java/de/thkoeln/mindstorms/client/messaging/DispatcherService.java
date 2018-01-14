package de.thkoeln.mindstorms.client.messaging;

import de.thkoeln.mindstorms.concurrency.ObservableRequest;
import de.thkoeln.mindstorms.server.controlling.operation.Opcode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DispatcherService
 */
public class DispatcherService {
    private final AtomicInteger count = new AtomicInteger(0);
    private final ExecutorService writerService = Executors.newSingleThreadExecutor();
    private final DataOutputStream dataOutputStream;
    private final OperandWriter operandWriter;
    private final ResultReceiverService resultReceiverService;

    public DispatcherService(DataOutputStream dataOutputStream, ResultReceiverService resultReceiverService) {
        this.dataOutputStream = dataOutputStream;
        this.resultReceiverService = resultReceiverService;
        this.operandWriter = new OperandWriter(this.dataOutputStream);
    }

    public void shutdown() {
        writerService.shutdown();
        try {
            writerService.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public ObservableRequest dispatch(final Method method, final Object... arguments) {
        final int id = count.getAndIncrement();
        final byte opcode = method.getAnnotation(Opcode.class).value();
        ObservableRequest observableRequest = new ObservableRequest(method){};

        resultReceiverService.registerRequest(id, observableRequest);
        writerService.submit(() -> {
            try {
                dataOutputStream.writeInt(id);
                dataOutputStream.writeByte(opcode);
                operandWriter.writeOperands(method, arguments);
                dataOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return observableRequest;
    }
}
