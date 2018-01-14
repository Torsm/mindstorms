package de.thkoeln.mindstorms.client;

import de.thkoeln.mindstorms.client.messaging.DispatcherService;
import de.thkoeln.mindstorms.client.messaging.ResultReceiverService;

import java.io.*;
import java.net.Socket;

/**
 * MindstormsClient
 */
public class MindstormsClient {
    private final Socket socket;
    private final DispatcherService dispatcherService;
    private final ResultReceiverService resultReceiverService;

    private MindstormsClient(Socket socket, DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        this.socket = socket;

        resultReceiverService = new ResultReceiverService(dataInputStream);
        resultReceiverService.start();

        this.dispatcherService = new DispatcherService(dataOutputStream, resultReceiverService);
    }

    public void exit() {
        try {
            dispatcherService.shutdown();
            resultReceiverService.shutdown();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MindstormsClient connect(String host, int port) {
        try {
            Socket socket = new Socket(host, port);
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            return new MindstormsClient(socket, dataInputStream, dataOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Couldn't connect to server");
        }
    }

    public DispatcherService getDispatcherService() {
        return dispatcherService;
    }
}
