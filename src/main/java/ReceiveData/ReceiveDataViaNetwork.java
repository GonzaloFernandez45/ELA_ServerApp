package ReceiveData;

import pojos.*;

import java.io.*;
import java.net.Socket;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReceiveDataViaNetwork {
    private DataInputStream dataInputStream;
    private Socket socket;
    public ReceiveDataViaNetwork(Socket socket){
        try {
            this.dataInputStream = new DataInputStream(socket.getInputStream());
        }catch (IOException ex){
            Logger.getLogger(ReceiveDataViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public String receiveString() {
        try {
            return dataInputStream.readUTF();
        } catch (IOException ex) {
            System.err.println("Error recibing String");
            Logger.getLogger(ReceiveDataViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public int receiveInt() {
        int message = 0;
        try {
            message = dataInputStream.readInt();
        } catch (IOException ex) {
            System.err.println("Error receiving int: " + ex.getMessage());
            ex.printStackTrace();
        }
        return message;
    }

    public void releaseResources() {
        try {
            if (dataInputStream != null) {
                dataInputStream.close();
            }
        } catch (IOException ex) {
            System.err.println("Error with resources: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


    public Patient recievePatient(){
        Patient patient = null;
        try {
            int id = dataInputStream.readInt();
            String name = dataInputStream.readUTF();
            String surname = dataInputStream.readUTF();
            Integer insurance = Integer.valueOf(dataInputStream.readUTF());
            patient = new Patient(id, name, surname, insurance);
        } catch (EOFException ex) {
            System.out.println("Data not correctly read.");
        } catch (IOException ex) {
            System.err.println("Error receiving patient data: " + ex.getMessage());
            ex.printStackTrace();
        }
        return patient;
    }

    public Doctor receiveDoctor() throws IOException{
        Doctor doctor = null;
        int id = dataInputStream.readInt();
        String name = dataInputStream.readUTF();
        String surname = dataInputStream.readUTF();
        String dni = dataInputStream.readUTF();
        Date birthDate = Date.valueOf(dataInputStream.readUTF());
        String sex = dataInputStream.readUTF();
        String email = dataInputStream.readUTF();
        doctor = new Doctor(id, name, surname, dni, birthDate, sex, email);

        return doctor;
    }

    public User recieveUser() {
        User u = null;
        try {
            // Recibir el ID del usuario
            int id = dataInputStream.readInt();

            // Recibir el nombre de usuario
            String username = dataInputStream.readUTF();

            // Recibir la contraseña encriptada
            String passwordEncripted = dataInputStream.readUTF();

            // Recibir el rol del usuario (como String) y crear el objeto Role
            String roleString = dataInputStream.readUTF();
            Role role = new Role(roleString);  // Asegúrate de que tu clase Role pueda manejar esto

            // Crear el objeto User con los datos recibidos
            u = new User(id, username, passwordEncripted.getBytes(), role);

        } catch (IOException ex) {
            Logger.getLogger(ReceiveDataViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
        return u;
    }

    public MedicalInformation receiveMedicalInformation() {
        MedicalInformation medicalInformation = null;
        try {
            int id = dataInputStream.readInt();  // Recibe el ID de la información médica
            Date reportDate = Date.valueOf(dataInputStream.readUTF());  // Recibe la fecha del informe

            int symptomsCount = dataInputStream.readInt();  // Número de síntomas
            List<Symptom> symptoms = new ArrayList<>();
            for (int i = 0; i < symptomsCount; i++) {
                int symptomId = dataInputStream.readInt();  // ID del síntoma
                // Solicitar el síntoma al servidor
                Symptom symptom = getSymptomFromServer(symptomId);  // Obtener el síntoma desde el servidor
                if (symptom != null) {
                    symptoms.add(symptom);
                }
            }

            // Recibe la lista de medicamentos
            int medicationCount = dataInputStream.readInt();
            List<String> medication = new ArrayList<>();
            for (int i = 0; i < medicationCount; i++) {
                medication.add(dataInputStream.readUTF());  // Agrega cada medicamento a la lista
            }

            // Recibe el feedback
            String feedback = dataInputStream.readUTF();  // Retroalimentación

            // Crea la instancia de MedicalInformation con todos los datos
            medicalInformation = new MedicalInformation(id, symptoms, reportDate, medication, feedback);

        } catch (IOException ex) {
            System.err.println("Error receiving medical information: " + ex.getMessage());
            ex.printStackTrace();
        }
        return medicalInformation;
    }

    //Obtiene el sintoma desde el servidor, se solicita la informacion.
    public Symptom getSymptomFromServer(int symptomId) {
        try {
            // Enviar el ID como entero binario
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeInt(symptomId);
            out.flush();

            // Recibir el objeto Symptom desde el servidor
            DataInputStream in = new DataInputStream(socket.getInputStream());
            int symptomIdFromServer = in.readInt();
            String symptomName = in.readUTF();  // Suponiendo que el nombre es un String
            // Aquí deberías recuperar el resto de los atributos del Symptom. Ajusta según tus necesidades
            Symptom symptom = new Symptom(symptomIdFromServer, symptomName);
            return symptom;
        } catch (IOException e) {
            System.err.println("Error fetching symptom from server: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

}
