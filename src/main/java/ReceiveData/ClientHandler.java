package ReceiveData;

import jdbc.*;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles a single client connection.
 * Creates DB managers, network I/O helpers and delegates logic to ServerControl.
 * Runs in its own thread for each connected client.
 */
public class ClientHandler implements Runnable {

    private Socket socket;

    /**
     * Builds a handler for a given client socket.
     *
     * @param socket connected client socket.
     */
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    /**
     * Thread entry point.
     * - Creates DB connection + manager objects.
     * - Wraps the socket with ReceiveDataViaNetwork and SendDataViaNetwork.
     * - Instantiates ServerControl and calls handleFirstMessage().
     * - On exit, decrements activeClients and releases all resources.
     */
    @Override
    public void run() {
        ConnectionManager connectionManager = new ConnectionManager();
        JDBCPatientManager patientManager = new JDBCPatientManager(connectionManager);
        JDBCUserManager userManager = new JDBCUserManager(connectionManager);
        JDBCSymptomManager symptomManager = new JDBCSymptomManager(connectionManager);
        JDBCMedicalInformationManager medicalInformationManager = new JDBCMedicalInformationManager(connectionManager);
        JDBCDoctorManager doctorManager = new JDBCDoctorManager(connectionManager);
        JDBCAdministratorManager administratorManager = new JDBCAdministratorManager(connectionManager);

        ReceiveDataViaNetwork receiveData = null;
        SendDataViaNetwork sendData = null;

        try {
            System.out.println("Handling client in thread: " + Thread.currentThread().getName());

            // Network I/O wrappers for this socket
            receiveData = new ReceiveDataViaNetwork(socket);
            sendData = new SendDataViaNetwork(socket);

            // Controller that implements the application protocol / menus
            ServerControl controller = new ServerControl(
                    socket, receiveData, sendData, patientManager, userManager,
                    symptomManager, medicalInformationManager, doctorManager, administratorManager
            );

            // Start handling the conversation with this client
            controller.handleFirstMessage();

        } catch (IOException e) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, "Error handling client", e);
        } finally {
            // Decrement global counter of active clients
            int remaining = Server.activeClients.decrementAndGet();
            System.out.println("Client disconnected. Remaining clients: " + remaining);

            // Release all resources related to this client
            try {
                if (receiveData != null) receiveData.releaseResources();
                if (sendData != null) sendData.releaseResources();

                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                if (connectionManager != null) {
                    connectionManager.close();
                }
            } catch (Exception ex) {
                System.out.println("Error releasing resources: " + ex.getMessage());
            }
        }
    }
}