package ReceiveData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import interfaces.*;
import pojos.*;

public class ClientHandler extends Thread {
    private Socket clientSocket;
    private SymptomManager symptomManager;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public ClientHandler(Socket socket, SymptomManager symptomManager) {
        this.clientSocket = socket;
        this.symptomManager = symptomManager;
    }

    @Override
    public void run() {
        try {
            // Crear los flujos de entrada y salida binaria
            dataInputStream = new DataInputStream(clientSocket.getInputStream());
            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());

            // Leer el ID del síntoma desde el cliente (4 bytes)
            int symptomId = dataInputStream.readInt();

            // Obtener el síntoma desde el SymptomManager
            Symptom symptom = symptomManager.getSymptomById(symptomId);

            // Enviar el síntoma al cliente (comenzamos por el ID, luego los atributos)
            // Si el objeto Symptom tiene propiedades como String, int, etc., lo mandas con writeUTF, writeInt, etc.
            if (symptom != null) {
                // Enviar el ID del síntoma primero
                dataOutputStream.writeInt(symptom.getId());

                // Luego enviar otros atributos del Symptom como datos binarios
                dataOutputStream.writeUTF(symptom.getDescription());  // Si el nombre es String
                // Agregar otros atributos si los tienes, por ejemplo:
                // dataOutputStream.writeUTF(symptom.getDescription());  // Ejemplo
            } else {
                // Enviar un código de error si no se encuentra el síntoma
                dataOutputStream.writeInt(-1); // Código de error para "No encontrado"
            }

            dataOutputStream.flush();  // Asegurarse de que los datos se envíen

        } catch (IOException e) {
            System.err.println("Error handling client request: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Liberar recursos
            try {
                releaseResources();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void releaseResources() {
        try {
            if (dataInputStream != null) {
                dataInputStream.close();
            }
            if (dataOutputStream != null) {
                dataOutputStream.close();
            }
        } catch (IOException ex) {
            System.err.println("Error with resources: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}

