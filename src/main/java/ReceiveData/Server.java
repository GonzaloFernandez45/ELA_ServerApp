package ReceiveData;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Main TCP server for the telemedicine application.
 * Listens on a port, accepts client connections and starts a ClientHandler thread for each.
 * Can be shut down remotely via stopServer(), typically from an admin action.
 */
public class Server {

    private int port;
    private static volatile boolean running = true;
    private static ServerSocket serverSocket;

    // Thread-safe counter of currently connected clients
    public static final AtomicInteger activeClients = new AtomicInteger(0);

    // Password used to authorize server shutdown (checked elsewhere)
    public static final String SHUTDOWN_PASSWORD = "admin";

    /**
     * Creates a server bound to the given port.
     *
     * @param port port where the server will listen for clients.
     */
    public Server(int port) {
        this.port = port;
    }

    /**
     * Starts the server loop:
     * - Opens the ServerSocket.
     * - Accepts new clients while running == true.
     * - For each connection, increments activeClients and launches a ClientHandler in a new thread.
     * When stopServer() is called, the ServerSocket is closed and the loop exits.
     */
    public void start() {
        System.out.println("Starting server on port " + port + "...");

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server listening on port " + port);

            while (running) {
                try {
                    // Blocks until a client connects (unless serverSocket is closed)
                    Socket socket = serverSocket.accept();

                    // New client connected → increment counter
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
            // Ensure resources are released when loop finishes
            stop();
        }
    }

    /**
     * Static method used to stop the main server loop.
     * - Sets running = false so the accept loop ends.
     * - Closes the ServerSocket to unblock accept().
     * Typically invoked from an admin command.
     */
    public static void stopServer() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close(); // Breaks the accept() blocking call
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Server shutdown initiated.");
    }

    /**
     * Instance-level stop method, delegates to the static shutdown logic.
     */
    public void stop() {
        stopServer();
    }
}

