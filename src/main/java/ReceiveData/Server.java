package ReceiveData;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import interfaces.*;
import jdbc.*;

public class Server {
    private int port;
    private SymptomManager symptomManager;

    public Server(int port, SymptomManager symptomManager) {
        this.port = port;
        this.symptomManager = symptomManager;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();  // Acepta una conexión entrante
                System.out.println("New client connected");

                // Crea un nuevo manejador de cliente para cada conexión
                new ClientHandler(clientSocket, symptomManager).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ConnectionManager manager = new ConnectionManager();
        SymptomManager symptomManager = new JDBCSymptomManager(manager);  // Crear el manejador de síntomas
        Server server = new Server(12345, symptomManager);  // Puerto 12345
        server.start();  // Inicia el servidor
    }
}

