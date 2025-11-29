package ReceiveData;

import pojos.*;

import java.io.*;
import java.net.Socket;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import pojos.*;

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
        ;
        try {
            return dataInputStream.readInt();
        } catch (IOException ex) {
            System.err.println("Error receiving int: " + ex.getMessage());
            return -1;
        }

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
            System.out.println("Receiving patient data...");
            String name = dataInputStream.readUTF();
            String surname = dataInputStream.readUTF();
            String dni = dataInputStream.readUTF();
            Date birthDate = Date.valueOf(dataInputStream.readUTF());
            String sex = dataInputStream.readUTF();
            int phone = dataInputStream.readInt();
            String email = dataInputStream.readUTF();
            int insurance = dataInputStream.readInt();
            patient = new Patient(name, surname, dni, birthDate, sex, phone, email, insurance);
            return patient;
        } catch (EOFException ex) {
            System.out.println("Data not correctly read.");
        } catch (IOException ex) {
            System.err.println("Error receiving patient data: " + ex.getMessage());
            ex.printStackTrace();
        }
        return patient;
    }

    public Administrator recieveAdmin(){
        Administrator administrator= null;
        try {
            System.out.println("Receiving administrator data...");;
            String email = dataInputStream.readUTF();
            String dni = dataInputStream.readUTF();
            administrator = new Administrator(dni,email);
            return administrator;
        } catch (EOFException ex) {
            System.out.println("Data not correctly read.");
        } catch (IOException ex) {
            System.err.println("Error receiving patient data: " + ex.getMessage());
            ex.printStackTrace();
        }
        return administrator;
    }


    public Doctor receiveDoctor(){
        Doctor doctor = null;
        try {
            System.out.println("Receiving doctor data...");
            String name = dataInputStream.readUTF();
            String surname = dataInputStream.readUTF();
            String DNI = dataInputStream.readUTF();
            java.sql.Date birthDate = Date.valueOf(dataInputStream.readUTF());
            String sex = dataInputStream.readUTF();
            String email = dataInputStream.readUTF();
            doctor = new Doctor(name, surname, DNI, birthDate, sex, email);

        } catch (EOFException ex){
            System.out.println("Data not correctly read.");
        }catch(IOException e){
            System.err.println("Error receiving patient data: " + e.getMessage());
            e.printStackTrace();
        }
        return doctor;
    }

    public User recieveUser()
    {
        User u = null;
        try {
            String email = dataInputStream.readUTF();
            String role = dataInputStream.readUTF();
            byte[] psw = dataInputStream.readUTF().getBytes();

            Role r = new Role(role);
            u = new User(email, psw, r);
            return u;
        } catch (IOException ex) {
            Logger.getLogger(ReceiveDataViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
        return u;
    }


    //Obtiene los sintomas desde el servidor, se solicita la informacion.
    public List<Symptom> receiveSymptoms() throws IOException {
        // 1. Leer cuántos síntomas vienen
        int size = dataInputStream.readInt();

        List<Symptom> symptoms = new ArrayList<Symptom>();

        // 2. Leer cada síntoma en el mismo orden en el que se envió
        for (int i = 0; i < size; i++) {
            int id = dataInputStream.readInt();
            String description = dataInputStream.readUTF();

            // Ajusta esto al constructor/setters que tengas en tu clase Symptom
            Symptom symptom = new Symptom(id,description);
            symptoms.add(symptom);
        }

        return symptoms;
    }

    //Obtiene los sintomas desde el servidor, se solicita la informacion.
    public List<String> receiveMedications() throws IOException {
        // 1. Leer cuántos síntomas vienen
        int size = dataInputStream.readInt();

        List<String> medications = new ArrayList<String>();

        // 2. Leer cada síntoma en el mismo orden en el que se envió
        for (int i = 0; i < size; i++) {
            String medication = dataInputStream.readUTF();
            medications.add(medication);
        }

        return medications;
    }

    public MedicalInformation receiveMedicalInformation() {
        MedicalInformation medicalInformation = null;
        try {
            Date reportDate = Date.valueOf(dataInputStream.readUTF());  // Recibe la fecha del informe

            List<Symptom> symptoms = receiveSymptoms();

            List<String> medication = receiveMedications();

            // Crea la instancia de MedicalInformation con todos los datos
            medicalInformation = new MedicalInformation(symptoms, reportDate, medication);

        } catch (IOException ex) {
            System.err.println("Error receiving medical information: " + ex.getMessage());
            ex.printStackTrace();
        }
        return medicalInformation;
    }

    public List<MedicalInformation> receiveMedicalInformationList() {
        List<MedicalInformation> medicalInformationList = null;
        MedicalInformation medicalInformation = null;
        try {
            int size = dataInputStream.readInt();
            for (int i = 0; i < size; i++) {

                int id = dataInputStream.readInt();  // Recibe el ID de la información médica
                Date reportDate = Date.valueOf(dataInputStream.readUTF());// Recibe la fecha del informe
                int medicationSize = dataInputStream.readInt();
                List<String> medicationList = null;
                for (int j = 0; j < medicationSize; j++) {
                    String med = dataInputStream.readUTF();
                    medicationList.add(med);
                }
                int symptomsCount = dataInputStream.readInt();  // Número de síntomas
                List<Symptom> symptoms = new ArrayList<>();
                for (int j = 0; j < symptomsCount; j++) {
                    Symptom symptom = null;
                    int symptomId = dataInputStream.readInt();  // ID del síntoma
                    String description = dataInputStream.readUTF();
                    symptom.setId(symptomId);
                    symptom.setDescription(description);
                    symptoms.add(symptom);
                }
                String feedback = dataInputStream.readUTF();  // Retroalimentación

                // Crea la instancia de MedicalInformation con todos los datos
                medicalInformation = new MedicalInformation(id,symptoms,reportDate,medicationList,feedback);
                medicalInformationList.add(medicalInformation);
            }
        } catch (IOException ex) {
            System.err.println("Error receiving medical information: " + ex.getMessage());
            ex.printStackTrace();
        }
        return medicalInformationList;
    }


    public Signal receiveSignal() throws IOException {
        Signal signal = null;

        try {
            // 1. Recibir el TIPO (String) y convertirlo de nuevo a Enum
            String typeString = dataInputStream.readUTF();
            TypeSignal type = TypeSignal.valueOf(typeString);

            // 2. Recibir el ID del CLIENTE
            int clientId = dataInputStream.readInt();

            // Creamos el objeto con los datos básicos
            signal = new Signal(type, clientId);

            // 3. Recibir el TAMAÑO de la lista
            int size = dataInputStream.readInt();
            List<Integer> values = new ArrayList<>();

            // 4. Recibir los VALORES uno por uno
            for (int i = 0; i < size; i++) {
                int value = dataInputStream.readInt();
                values.add(value);
            }

            // Asignamos la lista llena al objeto
            signal.setValues(values);

        } catch (IllegalArgumentException e) {
            System.err.println("Error: Tipo de señal desconocido recibido.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error recibiendo la señal: " + e.getMessage());
            throw e; // Relanzamos para que el hilo principal sepa que hubo error
        }

        return signal;
    }

}
