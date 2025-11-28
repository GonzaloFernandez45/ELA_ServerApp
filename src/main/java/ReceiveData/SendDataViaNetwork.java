package ReceiveData;

import interfaces.MedicalInformationManager;
import interfaces.PatientManager;
import interfaces.SymptomManager;
import pojos.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SendDataViaNetwork {
    private DataOutputStream dataOutputStream;
    public SendDataViaNetwork(Socket socket){
        try {
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
        }catch (IOException ex){
            Logger.getLogger(SendDataViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void sendStrings(String message) throws IOException {

        try {
            dataOutputStream.writeUTF(message);
            dataOutputStream.flush();
        } catch (IOException e) {
            System.err.println("Error send String ");
        }
    }
    public void sendInt(Integer message){
        try{
            dataOutputStream.writeInt(message);
        }catch (IOException ex){
            Logger.getLogger(SendDataViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void sendPatient(Patient patient) throws IOException{
        System.out.println("Sending patient data...");
        dataOutputStream.writeInt(patient.getId());
        dataOutputStream.writeUTF(patient.getName());
        dataOutputStream.writeUTF(patient.getSurname());
        dataOutputStream.writeUTF(patient.getDni());
        dataOutputStream.writeUTF(String.valueOf(patient.getDateOfBirth()));
        dataOutputStream.writeUTF(patient.getSex());
        dataOutputStream.writeInt(patient.getPhone());
        dataOutputStream.writeUTF(patient.getEmail());
        dataOutputStream.writeInt(patient.getInsurance());
        dataOutputStream.flush();
    }
    public void sendAdmin(Administrator administrator) throws IOException{
        System.out.println("Sending Admin data...");
        dataOutputStream.writeUTF(administrator.getEmail());
        dataOutputStream.writeUTF(administrator.getDni());
        dataOutputStream.flush();
    }

    public void sendUser(User user) throws IOException{
        dataOutputStream.writeUTF(user.getEmail());
        dataOutputStream.writeUTF(String.valueOf(user.getRole()));
        byte[] password = user.getPassword();
        dataOutputStream.writeUTF(new String(password));
    }

    public void sendDoctor(Doctor doctor) throws IOException {
        System.out.println("Sending doctor data...");
        dataOutputStream.writeInt(doctor.getId());
        dataOutputStream.writeUTF(doctor.getName());
        dataOutputStream.writeUTF(doctor.getSurname());
        dataOutputStream.writeUTF(doctor.getDNI());
        dataOutputStream.writeUTF(String.valueOf(doctor.getBirthDate()));
        dataOutputStream.writeUTF(doctor.getSex());
        dataOutputStream.writeUTF(doctor.getEmail());
        dataOutputStream.flush();

    }

    public void sendMedicalInformation(MedicalInformation medicalInformation) throws IOException {

        //Enviar id
        dataOutputStream.writeInt(medicalInformation.getId());

        dataOutputStream.writeUTF(String.valueOf(medicalInformation.getReportDate()));
        // Enviar la lista de síntomas
        sendSymptoms(medicalInformation.getSymptoms());

        // Enviar la lista de medicamentos
        sendMedications(medicalInformation.getMedication());

        // Enviar el feedback
        String feedback = medicalInformation.getFeedback();
        if (feedback == null) {
            feedback = "";  // Si feedback es null, enviar una cadena vacía
        }
        dataOutputStream.writeUTF(feedback);

        dataOutputStream.flush();
    }

    public void sendSymptoms(List<Symptom> symptoms) throws IOException {
        // 1. Enviar cuántos síntomas vienen
        dataOutputStream.writeInt(symptoms.size());

        // 2. Enviar cada síntoma (ajusta los campos a lo que tenga tu clase Symptom)
        for (Symptom symptom : symptoms) {
            dataOutputStream.writeInt(symptom.getId());
            dataOutputStream.writeUTF(symptom.getDescription());
            // si tu Symptom tiene más cosas, las vas escribiendo aquí en el mismo orden
        }

        dataOutputStream.flush();
    }

    public void releaseResources() {
        try {
            if (dataOutputStream != null) {
                dataOutputStream.close();
            }
        } catch (IOException ex) {
            System.err.println("Error with resources: " + ex.getMessage());
            Logger.getLogger(SendDataViaNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public void sendMedications(List<String> medications) throws IOException {
        // 1. Enviar cuántos síntomas vienen
        dataOutputStream.writeInt(medications.size());

        // 2. Enviar cada síntoma (ajusta los campos a lo que tenga tu clase Symptom)
        for (String medication : medications) {
            dataOutputStream.writeUTF(medication);
            // si tu Symptom tiene más cosas, las vas escribiendo aquí en el mismo orden
        }

        dataOutputStream.flush();
    }

    public void sendMedicalInformationList(List<MedicalInformation> medicalInformation) throws IOException {

        dataOutputStream.writeInt(medicalInformation.size());

        for (MedicalInformation mi : medicalInformation) {
            sendMedicalInformation(mi);
        }
        // Asegurarse de que los datos se escriban completamente
        dataOutputStream.flush();
    }

    public void sendSignalList(List<Signal> signals) throws IOException {
        // 1. Enviar cantidad de señales
        dataOutputStream.writeInt(signals.size());

        // 2. Enviar datos básicos de cada una
        for (Signal s : signals) {
            dataOutputStream.writeInt(s.getRecordId()); // ID único en la DB
            dataOutputStream.writeUTF(s.getType().toString()); // Tipo (EMG/ACC)

            // Enviamos la fecha como String. Si es null, enviamos "Unknown"
            String dateStr = (s.getDate() != null) ? s.getDate().toString() : "Unknown Date";
            dataOutputStream.writeUTF(dateStr);
        }
        dataOutputStream.flush();
    }

    public void sendSignal(Signal signal) throws IOException {
        List<Integer> values = signal.getValues();

        // 1. Tamaño de la lista
        dataOutputStream.writeInt(values.size());

        // 2. Valores uno a uno
        for (Integer val : values) {
            dataOutputStream.writeInt(val);
        }

        // 3. Nombre del archivo (Necesario para que el Doctor genere el PNG con el mismo nombre)
        String fileName = (signal.getSignalFilename() != null) ? signal.getSignalFilename() : "signal_temp.txt";
        dataOutputStream.writeUTF(fileName);

        // 4. Tipo de señal
        dataOutputStream.writeUTF(signal.getType().toString());

        dataOutputStream.flush();
    }

}
