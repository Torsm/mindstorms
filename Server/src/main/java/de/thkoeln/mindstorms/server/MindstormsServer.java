package de.thkoeln.mindstorms.server;

import de.thkoeln.mindstorms.server.controlling.EV3ServerController;
import de.thkoeln.mindstorms.server.controlling.operation.OpcodeResolver;
import de.thkoeln.mindstorms.server.messaging.RequestReceiverService;
import de.thkoeln.mindstorms.server.messaging.ResponderService;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * MindstormsServer
 */
public class MindstormsServer {
	public final static int PORT = 19999;
    private final ServerSocket serverSocket;
    
    private MindstormsServer(ServerSocket serverSocket) {
    		this.serverSocket = serverSocket;
    }

    private void listen() {
		EV3ServerController controller = new EV3ServerController();
        OpcodeResolver opcodeResolver = new OpcodeResolver(controller);

		while (true) {
			try {
				Socket socket = serverSocket.accept();

				ResponderService responderService = new ResponderService(socket.getOutputStream(), controller);
				RequestReceiverService requestReceiverService = new RequestReceiverService(socket.getInputStream(), responderService, opcodeResolver);

				requestReceiverService.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    }

    public static void run(int port) {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			new MindstormsServer(serverSocket).listen();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not create serversocket");
		}
    }
}
