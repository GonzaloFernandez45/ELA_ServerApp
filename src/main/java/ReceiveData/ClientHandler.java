package ReceiveData;

import jdbc.*;
import java.io.IOException;
import java.net.Socket;


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

            ServerControl controller = new ServerControl(
                    socket, receiveData, sendData, patientManager, userManager,
                    symptomManager, medicalInformationManager, doctorManager, administratorManager
            );

            controller.handleFirstMessage();

        } catch (IOException e) {

        } finally {

            int remaining = Server.activeClients.decrementAndGet();
            System.out.println("Client disconnected. Remaining clients: " + remaining);

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