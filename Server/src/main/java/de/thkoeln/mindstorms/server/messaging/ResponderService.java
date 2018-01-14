package de.thkoeln.mindstorms.server.messaging;

import de.thkoeln.mindstorms.concurrency.ObservableRequest;
import de.thkoeln.mindstorms.server.controlling.EV3ServerController;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ResponderService
 */
public class ResponderService {
    private final ExecutorService methodInvokationService = Executors.newFixedThreadPool(5);
    private final ExecutorService writerService = Executors.newSingleThreadExecutor();
    private final DataOutputStream dataOutputStream;
    private final EV3ServerController controller;

    public ResponderService(OutputStream outputStream, EV3ServerController controller) {
        this.dataOutputStream = new DataOutputStream(outputStream);
        this.controller = controller;
    }

    public void submit(final int id, final Method method, final Object[] parameters) {
        methodInvokationService.submit(new Runnable() {
            @Override
            @SuppressWarnings("unchecked")
            public void run() {
                try {
                    final Class<?> returnType = (Class) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
                    ((ObservableRequest) method.invoke(controller, parameters)).onComplete(new ObservableRequest.CompletionListener() {
                        @Override
                        public void onCompleted(final Object result) {
                            writerService.submit(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        dataOutputStream.writeInt(id);
                                        Objects.requireNonNull(ParameterType.getByType(returnType)).writeTo(dataOutputStream, result);
                                        dataOutputStream.flush();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (NullPointerException e) {
                                        e.printStackTrace();
                                        throw new RuntimeException(String.format("Return type not supported: %s", returnType.getCanonicalName()), e);
                                    }
                                }
                            });
                        }
                    });
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    // unexpected
                }
            }
        });
    }
}
