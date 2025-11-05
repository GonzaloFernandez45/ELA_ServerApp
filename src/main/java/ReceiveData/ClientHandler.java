package ReceiveData;

import java.io.DataInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;
import interfaces.*;
import pojos.*;

public class ClientHandler extends Thread {
    private Socket clientSocket;
    private SymptomManager symptomManager;

    public ClientHandler(Socket socket, SymptomManager symptomManager) {
        this.clientSocket = socket;
        this.symptomManager = symptomManager;
    }

    @Override
    public void run() {
        try {
            DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
            int symptomId = inputStream.readInt();  // Recibe el ID del síntoma

            // Obtener el síntoma desde el SymptomManager
            Symptom symptom = symptomManager.getSymptomById(symptomId);

            // Enviar el síntoma al cliente
            ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            outputStream.writeObject(symptom);
            outputStream.flush();

        } catch (IOException e) {
            System.err.println("Error handling client request: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();  // Cerrar la conexión cuando se termine de procesar
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
