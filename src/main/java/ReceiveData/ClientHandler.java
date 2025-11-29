package org.example;

import ReceiveData.ReceiveDataViaNetwork;
import ReceiveData.SendDataViaNetwork;
import ReceiveData.ServerControl;
import jdbc.*;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {

    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

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

            receiveData = new ReceiveDataViaNetwork(socket);
            sendData = new SendDataViaNetwork(socket);
            System.out.println("Socket accepted in handler: " + socket.getRemoteSocketAddress());

            ServerControl controller = new ServerControl(
                    socket,
                    receiveData,
                    sendData,
                    patientManager,
                    userManager,
                    symptomManager,
                    medicalInformationManager,
                    doctorManager,
                    administratorManager
            );

            controller.handleFirstMessage();

        } catch (IOException e) {
            Logger.getLogger(ClientHandler.class.getName())
                    .log(Level.SEVERE, "Error handling client", e);
        } finally {
            try {
                if (receiveData != null && sendData != null) {
                    sendData.releaseResources();
                    receiveData.releaseResources();
                }
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException ex) {
                System.out.println("Error releasing resources: " + ex.getMessage());
            }
            System.out.println("Client disconnected: " + socket);
        }
    }
}
