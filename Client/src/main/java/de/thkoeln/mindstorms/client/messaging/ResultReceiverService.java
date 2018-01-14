package de.thkoeln.mindstorms.client.messaging;

import de.thkoeln.mindstorms.concurrency.ObservableRequest;
import de.thkoeln.mindstorms.server.messaging.ParameterType;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * ResultReceiverService
 */
public class ResultReceiverService implements Runnable {
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final Map<Integer, ObservableRequest> requests = new ConcurrentHashMap<>();
    private final DataInputStream dataInputStream;
    private boolean shutdown;

    public ResultReceiverService(DataInputStream dataInputStream) {
        this.dataInputStream = dataInputStream;
    }

    public void registerRequest(int id, ObservableRequest observableRequest) {
        requests.put(id, observableRequest);
    }

    public void start() {
        executorService.scheduleWithFixedDelay(this, 1, 1, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        shutdown = true;
        try {
            executorService.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (shutdown && requests.isEmpty()) {
            executorService.shutdown();
            return;
        }

        try {
            int id = dataInputStream.readInt();
            ObservableRequest request = requests.get(id);
            try {
                Class<?> genericType = ((Class) ((ParameterizedType) request.getMethod().getGenericReturnType()).getActualTypeArguments()[0]);
                Object result = Objects.requireNonNull(ParameterType.getByType(genericType)).readFrom(dataInputStream);
                request.complete(result);
                requests.remove(id);
            } catch (IOException | ClassCastException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
