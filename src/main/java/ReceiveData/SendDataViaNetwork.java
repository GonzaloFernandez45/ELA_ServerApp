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



    public void sendMedicalInformationList(List<MedicalInformation> medicalInformation) throws IOException {
        dataOutputStream.writeInt(medicalInformation.size());
        for (MedicalInformation mi : medicalInformation) {
            dataOutputStream.writeInt(mi.getId());
            dataOutputStream.writeUTF(mi.getReportDate().toString());
            dataOutputStream.writeInt(mi.getMedication().size());
            for (String med : mi.getMedication()){
                dataOutputStream.writeUTF(med);
            }
            dataOutputStream.writeInt(mi.getSymptoms().size());
            for (Symptom s : mi.getSymptoms()) {
                dataOutputStream.writeInt(s.getId());
                dataOutputStream.writeUTF(s.getDescription());
            }
            dataOutputStream.writeUTF(mi.getFeedback());
        }
        // Asegurarse de que los datos se escriban completamente
        dataOutputStream.flush();
    }



    public void sendMedicalInformation(MedicalInformation medicalInformation) throws IOException {

        dataOutputStream.writeUTF(String.valueOf(medicalInformation.getReportDate()));
        // Enviar la lista de síntomas
        sendSymptoms(medicalInformation.getSymptoms());

        // Enviar la lista de medicamentos
        sendMedications(medicalInformation.getMedication());

        // Enviar el feedback
        dataOutputStream.writeUTF(medicalInformation.getFeedback());

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


}
