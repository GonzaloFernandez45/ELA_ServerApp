package ReceiveData;


import org.example.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private int port;
    private boolean running;

    public Server(int port) {
        this.port = port;
        this.running = true;
    }

    public void start() {
        System.out.println("Starting server on port " + port + "...");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);

            while (running) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket.getRemoteSocketAddress());

                ClientHandler clientHandler = new ClientHandler(socket);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }

        } catch (IOException e) {
            Logger.getLogger(Server.class.getName())
                    .log(Level.SEVERE, "Error in server main loop", e);
        }

        System.out.println("Server stopped.");
    }

    public void stop() {
        running = false;
    }
}

