package ReceiveData;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private int port;
    private static volatile boolean running = true;
    private static ServerSocket serverSocket;

    // Contador de clientes thread-safe
    public static final AtomicInteger activeClients = new AtomicInteger(0);

    // Contraseña para apagar el servidor
    public static final String SHUTDOWN_PASSWORD = "admin";

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        System.out.println("Starting server on port " + port + "...");

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server listening on port " + port);

            while (running) {
                try {
                    Socket socket = serverSocket.accept();

                    // Incrementamos clientes al conectar
                    activeClients.incrementAndGet();
                    System.out.println("New client connected. Active clients: " + activeClients.get());

                    ClientHandler clientHandler = new ClientHandler(socket);
                    Thread clientThread = new Thread(clientHandler);
                    clientThread.start();

                } catch (IOException e) {
                    if (!running) {
                        System.out.println("Server socket closed securely.");
                    } else {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, "Error accepting client", e);
                    }
                }
            }

        } catch (IOException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, "Error starting server", e);
        } finally {
            stop();
        }
    }

    // Método estático para parar el servidor desde el Admin
    public static void stopServer() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close(); // Esto rompe el bloqueo de accept()
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Server shutdown initiated.");
    }

    public void stop() {
        stopServer();
    }
}

