package ReceiveData;

import interfaces.*;
import jdbc.*;
import pojos.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.sql.Date;
import java.util.List;

public class ServerControl {

    private Socket socket;
    private ReceiveDataViaNetwork receiveDataViaNetwork;
    private SendDataViaNetwork sendDataViaNetwork;

    private PatientManager patientManager;
    private JDBCUserManager userManager;
    private SymptomManager symptomManager;
    private MedicalInformationManager medicalInformationManager;
    private DoctorManager doctorManager;
    private AdministratorManager administratorManager;

    public ServerControl(Socket socket, ReceiveDataViaNetwork receiveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork, PatientManager patientManager, JDBCUserManager userManager, SymptomManager symptomManager, MedicalInformationManager medicalInformationManager, DoctorManager doctorManager, AdministratorManager administratorManager) {
        this.socket = socket;
        this.receiveDataViaNetwork = receiveDataViaNetwork;
        this.sendDataViaNetwork = sendDataViaNetwork;
        this.patientManager = patientManager;
        this.userManager = userManager;
        this.symptomManager = symptomManager;
        this.medicalInformationManager = medicalInformationManager;
        this.doctorManager = doctorManager;
        this.administratorManager = administratorManager;
    }

    /**
     * Primer punto de entrada: lee el primer int (1=Patient, 2=Doctor, 3=Admin)
     * y redirige al menú correspondiente.
     */
    public void handleFirstMessage() throws IOException {
        int message = receiveDataViaNetwork.receiveInt();

        if (message == 1) {
            sendDataViaNetwork.sendStrings("PATIENT");
            patientMenu();
        } else if (message == 2) {
            sendDataViaNetwork.sendStrings("DOCTOR");
            doctorMenu();
        } else if (message == 3) {
            sendDataViaNetwork.sendStrings("ADMIN");
            adminMenu();
        } else {
            System.out.println("Unknown first message: " + message);
        }
    }

    private void patientMenu() throws IOException {
        try {
            boolean patientMenu = true;

            while (patientMenu) {
                int opcion = receiveDataViaNetwork.receiveInt();

                // Cliente desconectado
                if (opcion == -1) {
                    System.out.println("Patient menu: client disconnected (receiveInt = -1)");
                    patientMenu = false;
                    break;
                }

                switch (opcion) {
                    case 1:
                        logInPatient();
                        break;
                    case 2:
                        System.out.println("Patient register");
                        patientRegister();
                        break;
                    case 3:
                        patientMenu = false;
                        System.out.println("Patient disconnected");
                        break;
                    default:
                        System.out.println("Invalid option");
                        break;
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            releaseResources(receiveDataViaNetwork, sendDataViaNetwork, socket);
        }
    }

    private void patientRegister() throws IOException {
        try {
            String message = receiveDataViaNetwork.receiveString();
            System.out.println(message);

            if (message.equals("OK")) {
                System.out.println("Registering patient...");
                Patient patient = receiveDataViaNetwork.recievePatient();
                System.out.println(patient.toString());
                User user = receiveDataViaNetwork.recieveUser();
                System.out.println(user.toString());
                patientManager.addPatient(patient); //Primero añadir al paciente a la DB

                int patient_id = patientManager.getPatientIDFromEmail(patient.getEmail()); //Obtener su ID
                System.out.println("Patient ID from email = " + patient_id);
                user.setPatient_id(patient_id); //Asignar la Foreign Key al usuario


                userManager.addUser(user); //Añadir el usuario a la DB

                sendDataViaNetwork.sendStrings("SUCCESS");

                patient.setId(patient_id);
                menuPaciente(patient);

            } else {
                System.out.println("Error in register");

            }
        } catch (IOException ex) {
            System.out.println("Error or client disconnected");
            releaseResources(receiveDataViaNetwork, sendDataViaNetwork, socket);
        }
    }

    private void menuPaciente(Patient patient) throws IOException {
        try {
            boolean patientMenu = true;

            while (patientMenu) {
                int opcion = receiveDataViaNetwork.receiveInt();

                if (opcion == -1) {
                    System.out.println("menuPaciente: client disconnected (receiveInt = -1)");
                    patientMenu = false;
                    break;
                }

                switch (opcion) {
                    case 1:
                        System.out.println("Option 1: Insert medical info");
                        patientInsertMedicalInformartion(patient);
                        break;

                    case 2:
                        System.out.println("Option 2: Receiving Signal...");
                        patientReceiveAndSaveSignal(patient,receiveDataViaNetwork,sendDataViaNetwork);
                        break;

                    case 3:
                        System.out.println("Option 3: Sending doctor feedback");
                        patientSeeDoctorFeedback(patient);
                        break;

                    case 4:
                        System.out.println("Option 4: Change patient data");
                        updatePatientData();
                        break;

                    case 0:
                        System.out.println("Client disconnected (Option 0)");
                        patientMenu = false;
                        break;

                    default:
                        System.out.println("Invalid option received: " + opcion);
                        break;
                }
            }
        } catch (Exception ex) {
            System.out.println("Error in patient menu: " + ex.getMessage());
            releaseResources(receiveDataViaNetwork, sendDataViaNetwork, socket);
        }
    }


    private void logInPatient() throws IOException {
        try{
            sendDataViaNetwork.sendStrings("Patient log in");
            String message = receiveDataViaNetwork.receiveString();
            System.out.println(message);
            Role role = new Role("Patient");

            if (message.equals("OK")) {
                User user = receiveDataViaNetwork.recieveUser();

                boolean correctPassword = userManager.checkPassword(new String(user.getPassword()), user.getEmail());
                if (correctPassword) {
                    sendDataViaNetwork.sendStrings("SUCCESS");

                    int patient_id = patientManager.getPatientIDFromEmail(user.getEmail());
                    Patient patient = patientManager.getPatientbyId(patient_id);
                    user.setPatient_id(patient_id);

                    System.out.println(patient.toString());

                    sendDataViaNetwork.sendPatient(patient);
                    //
                    menuPaciente(patient);
                } else {
                    sendDataViaNetwork.sendStrings("ERROR");
                }
            } else {
                System.out.println("Error in login");
            }


        } catch (IOException e) {
            System.out.println("Error or client disconnected");
            releaseResources(receiveDataViaNetwork, sendDataViaNetwork, socket);
        }
    }

    private void releaseResources(ReceiveDataViaNetwork recieveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork, Socket socket) {
        if (sendDataViaNetwork != null && recieveDataViaNetwork != null) {
            sendDataViaNetwork.releaseResources();
            recieveDataViaNetwork.releaseResources();
        }
        try {
            socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void doctorMenu() throws IOException {
        try {
            boolean doctorMenu = true;

            while (doctorMenu) {
                int opcion = receiveDataViaNetwork.receiveInt();

                // Cliente desconectado
                if (opcion == -1) {
                    System.out.println("Doctor pre-login menu: client disconnected (receiveInt = -1)");
                    doctorMenu = false;
                    break;
                }

                switch (opcion) {
                    case 1:
                        System.out.println("Doctor Log in");
                        logInDoctor();
                        break;
                    case 2:
                        System.out.println("Doctor register");
                        doctorRegister();
                        break;
                    case 3:
                        doctorMenu = false;
                        System.out.println("Doctor disconnected");
                        break;
                    default:
                        System.out.println("Invalid option");
                        break;
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            releaseResources(receiveDataViaNetwork, sendDataViaNetwork, socket);
        }
    }

    private void doctorRegister() throws IOException {
        try {
            String message = receiveDataViaNetwork.receiveString();
            System.out.println(message);

            if (message.equals("OK")) {
                System.out.println("Registering Doctor...");
                Doctor doctor = receiveDataViaNetwork.receiveDoctor();
                System.out.println(doctor.toString());
                User user = receiveDataViaNetwork.recieveUser();
                System.out.println(user.toString());
                doctorManager.addDoctor(doctor); //Primero añadir al paciente a la DB

                int doctor_id = doctorManager.getDoctorIDFromEmail(doctor.getEmail()); //Obtener su ID

                user.setDoctor_id(doctor_id); //Asignar la Foreign Key al usuario
                userManager.addUser(user); //Añadir el usuario a la DB

                sendDataViaNetwork.sendStrings("SUCCESS");

                doctor.setId(doctor_id);

                System.out.println("Doctor requested patient list");;
                Patient patient = selectPatientForDoctor(doctor);

                menuDoctor(patient, doctor);

            } else {
                System.out.println("Error in register");

            }
        } catch (IOException ex) {
            System.out.println("Error or client disconnected");
            releaseResources(receiveDataViaNetwork, sendDataViaNetwork, socket);
        }
    }
    private Patient selectPatientForDoctor(Doctor doctor
    ) throws IOException {

        // 1. Obtener lista de pacientes
        List<Patient> patients = patientManager.listPatients();

        StringBuilder sb = new StringBuilder();
        sb.append("PATIENT LIST:\n");
        for (Patient p : patients) {
            sb.append("ID: ").append(p.getId())
                    .append(" | Name: ").append(p.getName()).append(" ").append(p.getSurname())
                    .append("\n");
        }

        // 2. Enviar lista al doctor
        sendDataViaNetwork.sendStrings(sb.toString());

        // 3. Recibir el ID seleccionado por el doctor
        int selectedId = receiveDataViaNetwork.receiveInt();

        System.out.println("Selecting patient...");
        // 4. Buscar paciente
        Patient selected = patientManager.getPatientbyId(selectedId);
        System.out.println(selected);

        if (selected != null) {
            System.out.println("Doctor selected patient: " + selected.getName());
            return selected;
        } else {
            sendDataViaNetwork.sendStrings("ERROR: Patient not found");
        }
        return selected;
    }

    private void logInDoctor() throws IOException {
        try{
            sendDataViaNetwork.sendStrings("Doctor log in");
            String message = receiveDataViaNetwork.receiveString();
            System.out.println(message);
            Role role = new Role("Doctor");

            if(message.equals("OK")){
                User user = receiveDataViaNetwork.recieveUser();
                boolean correctPassword = userManager.checkPassword(new String(user.getPassword()), user.getEmail());
                if(correctPassword){
                    sendDataViaNetwork.sendStrings("SUCCESS");

                    int doctor_id = doctorManager.getDoctorIDFromEmail(user.getEmail());
                    Doctor doctor = doctorManager.getDoctorbyId(doctor_id);
                    user.setDoctor_id(doctor_id);
                    System.out.println(doctor.toString());
                    sendDataViaNetwork.sendDoctor(doctor);

                    System.out.println("Doctor requested patient list");;
                    Patient patient = selectPatientForDoctor(doctor);


                    menuDoctor(patient, doctor);

                }else{
                    sendDataViaNetwork.sendStrings("ERROR");}
            }else{
                System.out.println("Error in login");
            }


        } catch (IOException e) {
            System.out.println("Error or client disconnected");
            releaseResources(receiveDataViaNetwork,sendDataViaNetwork,socket);
        }
    }

    private void menuDoctor(Patient patient, Doctor doctor) throws IOException {
        try{

            boolean doctorMenu = true;
            while (doctorMenu) {
                int opcion = receiveDataViaNetwork.receiveInt();

                if (opcion == -1) {
                    System.out.println("menuDoctor: client disconnected (receiveInt = -1)");
                    doctorMenu = false;
                    break;
                }

                switch (opcion) {
                    case 1:
                        System.out.println("SELECTED: View patient details");
                        viewPatient(patient);
                        doctorViewMedicalInformation(patient,doctor);
                        break;
                    case 2:
                        System.out.println("SELECTED: Add feedback");
                        //addFeedback(socket, recieveDataViaNetwork,sendDataViaNetwork);
                        selectAndUpdateFeedback();
                        break;
                    case 3:
                        System.out.println("SELECTED: View recorded signal");
                        doctorViewSignals();
                        break;

                    case 0:
                        System.out.println("0. Exit");
                        doctorMenu = false;
                        break;
                    default:
                        System.out.println("Invalid option");
                        break;
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            releaseResources(receiveDataViaNetwork, sendDataViaNetwork, socket);
        }
    }


    public void viewPatient(Patient patient) throws IOException {
        System.out.println("The doctor has requested the information of the patient:" + patient.getId());
        System.out.println(patient);
        if(patient != null){
            sendDataViaNetwork.sendPatient(patient);
        }
        else {
            sendDataViaNetwork.sendStrings("ERROR - PATIENT NOT FOUND");
        }

    }

    public void viewPatientMedInfo() throws IOException {
        int patient_id = receiveDataViaNetwork.receiveInt();
        System.out.println("The doctor has requested the Medical information of the patient:");
        ConnectionManager connectionManager = new ConnectionManager();
        JDBCMedicalInformationManager medicalInformationManager = new JDBCMedicalInformationManager(connectionManager);
        List<MedicalInformation> medicalInformationList = medicalInformationManager.getMedicalInfoByPatientId(patient_id);
        sendDataViaNetwork.sendMedicalInformationList(medicalInformationList);
    }

    public void selectAndUpdateFeedback() throws IOException {

        // 1. Recibir ID del paciente
        int patientId = receiveDataViaNetwork.receiveInt();

        // 2. Obtener lista de la base de datos
        List<MedicalInformation> medicalInfoList = medicalInformationManager.getMedicalInfoByPatientId(patientId);

        // 3. Comprobar si está vacía
        if (medicalInfoList.isEmpty()) {
            sendDataViaNetwork.sendStrings("No medical records found for this patient.");
            return;
        }

        // 4. Enviar lista al doctor
        StringBuilder records = new StringBuilder("Select a record to update feedback:\n");
        // Usamos un bucle for-i tradicional para tener control total del índice
        for (int i = 0; i < medicalInfoList.size(); i++) {
            MedicalInformation m = medicalInfoList.get(i);
            // El usuario ve opciones del 1 al N
            records.append(i + 1)
                    .append(". Date: ").append(m.getReportDate())
                    .append(" | Current Feedback: ").append(m.getFeedback() == null ? "None" : m.getFeedback())
                    .append("\n");
        }
        sendDataViaNetwork.sendStrings(records.toString());

        // 5. Recibir selección (1-based index)
        int selectedIndex = receiveDataViaNetwork.receiveInt();

        // 6. VALIDACIÓN CORREGIDA
        // El usuario elige entre 1 y size().
        if (selectedIndex < 1 || selectedIndex > medicalInfoList.size()) {
            sendDataViaNetwork.sendStrings("Invalid selection. Please try again.");
            // IMPORTANTE: Consumimos el string del feedback "fantasma" que el cliente envía justo después
            // para no desincronizar el socket en la siguiente vuelta.
            receiveDataViaNetwork.receiveString();
            return;
        }

        // 7. Obtener registro (convertir a 0-based index)
        MedicalInformation selectedRecord = medicalInfoList.get(selectedIndex - 1);

        // 8. Recibir nuevo feedback
        String newFeedback = receiveDataViaNetwork.receiveString();

        // 9. Actualizar en DB
        boolean success = medicalInformationManager.updateFeedback(selectedRecord.getId(), newFeedback);

        // 10. Responder
        if (success) {
            sendDataViaNetwork.sendStrings("Feedback updated successfully!");
        } else {
            sendDataViaNetwork.sendStrings("Error updating feedback in database.");
        }
    }


    public void addFeedback() throws IOException {
        int patient_id = receiveDataViaNetwork.receiveInt();
        ConnectionManager conMan = new ConnectionManager();
        JDBCPatientManager patientManager = new JDBCPatientManager(conMan);
        JDBCMedicalInformationManager medicalInformationManager = new JDBCMedicalInformationManager(conMan);
        List<MedicalInformation> medicalInformation = medicalInformationManager.getMedicalInfoByPatientId(patient_id);
        // Enviamos cada dato relevante de med info
        sendDataViaNetwork.sendInt(medicalInformation.size());
        for (MedicalInformation mi : medicalInformation) {
            sendDataViaNetwork.sendInt(mi.getId());
            sendDataViaNetwork.sendStrings(mi.getReportDate().toString());
        }
        int mi_Id = receiveDataViaNetwork.receiveInt();
        String feedback = receiveDataViaNetwork.receiveString();

        medicalInformationManager.updateMedicalInformation(patient_id, feedback);// Procesar y guardar el feedback


    }


    public void updatePatientData() throws IOException {
        int patientId = receiveDataViaNetwork.receiveInt();

        String newName = receiveDataViaNetwork.receiveString();
        String newSurname = receiveDataViaNetwork.receiveString();
        int newPhone = receiveDataViaNetwork.receiveInt();
        String newEmail = receiveDataViaNetwork.receiveString();
        String newdni = receiveDataViaNetwork.receiveString();
        String newSex = receiveDataViaNetwork.receiveString();
        int newInsurance = receiveDataViaNetwork.receiveInt();

        JDBCPatientManager pm = new JDBCPatientManager(new ConnectionManager());
        boolean updated = false;

        if (newName != null && !newName.isEmpty()) {
            pm.updatePatientName(patientId, newName);
            updated = true;
        }

        if (newSurname != null && !newSurname.isEmpty()) {
            pm.updatePatientSurname(patientId, newSurname);
            updated = true;
        }

        if (newPhone != -1) {
            pm.updatePatientPhone(patientId, newPhone);
            updated = true;
        }

        if (newEmail != null && !newEmail.isEmpty()) {
            pm.updatePatientEmail(patientId, newEmail);
            updated = true;
        }
        if (newdni != null && !newdni.isEmpty()) {
            pm.updatePatientDNI(patientId, newdni);
            updated = true;
        }
        if (newSex != null && !newSex.isEmpty()) {
            pm.updatePatientSex(patientId, newSex);
            updated = true;
        }

        if (newInsurance != -1) {
            pm.updatePatientInsurance(patientId, newInsurance);
            updated = true;
        }

        if (updated) {
            sendDataViaNetwork.sendStrings("Patient data updated successfully.");
        } else {
            sendDataViaNetwork.sendStrings("No data updated.");
        }
    }


    private void patientInsertMedicalInformartion(Patient patient) throws IOException {
        try {
            String message = receiveDataViaNetwork.receiveString();
            if (message.equals("SEND SYMPTOMS")) {
                sendDataViaNetwork.sendStrings("OK");

                //lega hasta aqui
                List<Symptom> listSymptoms = symptomManager.listSymptoms();
                System.out.println("Sending symptoms: " + listSymptoms);
                System.out.println("Sending symptoms to client, number of symptoms: " + listSymptoms.size());
                for (Symptom symptom : listSymptoms) {
                    System.out.println("Symptom ID: " + symptom.getId() + ", Description: " + symptom.getDescription());
                }
                sendDataViaNetwork.sendSymptoms(listSymptoms);

                System.out.println("Medical information in process");

                MedicalInformation medicalInformation = receiveDataViaNetwork.receiveMedicalInformation();
                medicalInformation.setPatient_id(patient.getId());
                System.out.println(medicalInformation);
                if (medicalInformation != null) {
                    sendDataViaNetwork.sendStrings("RECEIVED MEDICAL INFORMATION");
                    medicalInformationManager.insertMedicalInformation(medicalInformation); //añadimos medical info a la DB

                    //insertar en symptom_medicalinfromation
                    int medicalInfoId = medicalInformationManager.getMedicalInformationByDate(medicalInformation.getReportDate(), medicalInformation.getPatient_id()).getId();
                    List<Symptom> symptoms = medicalInformation.getSymptoms();

                    for (Symptom symptom : symptoms) {
                        medicalInformationManager.insertSymptomMedicalInformation(medicalInfoId, symptom);
                    }

                } else {
                    sendDataViaNetwork.sendStrings("ERROR");
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error or client disconnected");
            releaseResources(receiveDataViaNetwork, sendDataViaNetwork, socket);
        }



    }

    private void doctorViewMedicalInformation(Patient patient,Doctor doctor) throws IOException {
        try {

            // Retrieve all medical information for the patient
            List<MedicalInformation> medicalInfos = medicalInformationManager.getMedicalInfoByPatientId(patient.getId());

            if (medicalInfos.isEmpty()) {
                //sendDataViaNetwork.sendStrings("ERROR: No medical information found");
                System.out.println("ERROR: No medical information found");
                sendDataViaNetwork.sendInt(medicalInfos.size()); //should be 0

            }else{
                //send List to Doctor


                System.out.println("Sending medical information to doctor...");
                sendDataViaNetwork.sendInt(medicalInfos.size());
                // Send each medical information object to the doctor
                for (MedicalInformation info : medicalInfos) {
                    List<Symptom> symptoms = symptomManager.getSymptomsOfMedicalInformation(info.getId());
                    System.out.println(symptoms);
                    info.setSymptoms(symptoms);
                    System.out.println(medicalInfos);
                    sendDataViaNetwork.sendMedicalInformation(info);
                }

                String response = receiveDataViaNetwork.receiveString();

                if (response != null && response.equals("RECEIVED MEDICAL INFORMATION")) {
                    System.out.println("Doctor confirmed reception of medical information.");
                }
            }



        } catch (Exception e) {
            e.printStackTrace();
            sendDataViaNetwork.sendStrings("ERROR: Could not fetch medical information");
        }
    }

private void patientSeeDoctorFeedback(Patient patient) {

    try {
        String message = receiveDataViaNetwork.receiveString();

        if (message.equals("REQUEST FEEDBACK")) {
            System.out.println("Received request feedback, sending list of reports");
            sendDataViaNetwork.sendStrings("OK");

            // 1. Obtener todos los medical reports de ese paciente
            List<MedicalInformation> medicalList =
                    medicalInformationManager.getMedicalInfoByPatientId(patient.getId());
            // Ajusta este método al que tengas en tu manager

            // 2. Enviar cuántos hay
            int size = (medicalList == null) ? 0 : medicalList.size();
            sendDataViaNetwork.sendInt(size);

            if (size == 0) {
                System.out.println("No medical information for patient " + patient.getId());
                return; // no hay nada más que hacer
            }

            // 3. Enviar, para cada informe, "fecha | Symptoms: xxx, yyy, zzz"
            for (MedicalInformation mi : medicalList) {

                // Cargar síntomas de ese informe
                List<Symptom> symptomsOfMI =
                        symptomManager.getSymptomsOfMedicalInformation(mi.getId());

                StringBuilder symptomsText = new StringBuilder();
                if (symptomsOfMI != null && !symptomsOfMI.isEmpty()) {
                    for (int i = 0; i < symptomsOfMI.size(); i++) {
                        Symptom s = symptomsOfMI.get(i);
                        symptomsText.append(s.getDescription());
                        if (i < symptomsOfMI.size() - 1) {
                            symptomsText.append(", ");
                        }
                    }
                } else {
                    symptomsText.append("None");
                }

                // Ejemplo de línea: "2025-11-25 | Symptoms: Muscle fatigue, Difficulty swallowing"
                String line = mi.getReportDate().toString() + " | Symptoms: " + symptomsText;
                sendDataViaNetwork.sendStrings(line);
            }

            // 4. Recibir la selección del paciente (1..size)
            int selection = receiveDataViaNetwork.receiveInt();
            System.out.println("Patient selected report index: " + selection);

            if (selection < 1 || selection > size) {
                System.out.println("Invalid selection received from patient.");
                sendDataViaNetwork.sendStrings("ERROR");
                return;
            }

            // 5. Escoger el MedicalInformation correspondiente
            MedicalInformation selectedMI = medicalList.get(selection - 1);

            // Cargar síntomas completos para el informe seleccionado (si quieres, de nuevo)
            List<Symptom> symptomsOfSelected =
                    symptomManager.getSymptomsOfMedicalInformation(selectedMI.getId());
            selectedMI.setSymptoms(symptomsOfSelected);

            // 6. Enviar ese MedicalInformation al cliente
            sendDataViaNetwork.sendMedicalInformation(selectedMI);

            // 7. Esperar confirmación
            String response = receiveDataViaNetwork.receiveString();
            if (response != null && response.equals("RECEIVED MEDICAL INFORMATION")) {
                System.out.println("Patient confirmed reception of doctor feedback.");
            }

        } else {
            sendDataViaNetwork.sendStrings("ERROR");
        }
    } catch (Exception e) {
        e.printStackTrace();
        System.out.println("Error or client disconnected");
        releaseResources(receiveDataViaNetwork, sendDataViaNetwork, socket);
    }
}


    private void adminRegister() throws IOException {
        try {
            String message = receiveDataViaNetwork.receiveString();
            System.out.println(message);

            if (message.equals("OK")) {
                System.out.println("Registering administrator...");
                Administrator administrator = receiveDataViaNetwork.recieveAdmin();
                System.out.println(administrator.toString());
                User user = receiveDataViaNetwork.recieveUser();
                System.out.println(user.toString());
                administratorManager.insertAdministrator(administrator); //Primero añadir al paciente a la DB

                int admin_id = administratorManager.getAdministratorByEmail(administrator.getEmail()).getId(); //Obtener su ID
                System.out.println("Admin ID from email = " + admin_id);
                user.setAdmin_id(admin_id); //Asignar la Foreign Key al usuario
                userManager.addUser(user); //Añadir el usuario a la DB

                sendDataViaNetwork.sendStrings("SUCCESS");
                administrator.setId(admin_id);
                menuAdmin(administrator);

            } else {
                System.out.println("Error in register");

            }
        } catch (IOException ex) {
            System.out.println("Error or client disconnected");
            releaseResources(receiveDataViaNetwork, sendDataViaNetwork, socket);
        }
    }

    private void logInAdmin() throws IOException {
        try {
            sendDataViaNetwork.sendStrings("Administrator log in");
            String message = receiveDataViaNetwork.receiveString();
            System.out.println(message);
            Role role = new Role("Administrator");

            if (message.equals("OK")) {
                User user = receiveDataViaNetwork.recieveUser();
                boolean correctPassword = userManager.checkPassword(new String(user.getPassword()), user.getEmail());
                if (correctPassword) {
                    sendDataViaNetwork.sendStrings("SUCCESS");

                    int admin_id = administratorManager.getAdministratorByEmail(user.getEmail()).getId();
                    Administrator administrator = administratorManager.getAdministratorById(admin_id);
                    user.setAdmin_id(admin_id);

                    System.out.println(administrator.toString());

                    sendDataViaNetwork.sendAdmin(administrator);
                    //
                    menuAdmin(administrator);
                } else {
                    sendDataViaNetwork.sendStrings("ERROR");
                }
            } else {
                System.out.println("Error in login");
            }


        } catch (IOException e) {
            System.out.println("Error or client disconnected");
            releaseResources(receiveDataViaNetwork, sendDataViaNetwork, socket);
        }
    }

    private void adminMenu() throws IOException {
        try {
            boolean adminMenuLoop = true;
            while (adminMenuLoop) {
                int opcion = receiveDataViaNetwork.receiveInt();

                if (opcion == -1) {
                    adminMenuLoop = false;
                    break;
                }

                switch (opcion) {
                    case 1: // Log In
                        logInAdmin();
                        break;
                    case 2: // Register
                        adminRegister();
                        break;
                    case 3: // Exit
                        adminMenuLoop = false;
                        break;
                }
            }
        } catch (IOException ex) {
            // Error handling
        }
    }

    // Este es el menú CUANDO YA ESTÁ LOGUEADO
    private void menuAdmin(Administrator administrator) throws IOException {
        try {
            boolean menuLoop = true;
            while (menuLoop) {
                int opcion = receiveDataViaNetwork.receiveInt();
                switch (opcion) {
                    case 1: // STOP SERVER
                        System.out.println("Admin requested shutdown.");
                        handleShutdownRequest();
                        // Si se apaga el servidor, salimos del bucle
                        if (!Server.activeClients.equals(new java.util.concurrent.atomic.AtomicInteger(0))) {
                            // Nota: Como el socket se cerrará, esto lanzará excepción y saldrá solo
                        }
                        break;

                    case 0: // Exit
                        menuLoop = false;
                        break;
                }
            }
        } catch (Exception ex) {
            // Ignoramos errores de cierre si estamos apagando
        }
    }

    private void handleShutdownRequest() throws IOException {
        // 1. Contamos clientes
        int currentClients = Server.activeClients.get();

        // Si hay más de 1 cliente (el admin cuenta como 1), pedimos confirmación extra
        if (currentClients > 1) {
            sendDataViaNetwork.sendStrings("WARNING: There are " + (currentClients - 1) + " other clients connected. Force stop? (yes/no)");

            // --- CORRECCIÓN AQUÍ: EL SERVIDOR DEBE ESPERAR TU RESPUESTA ---
            String confirmation = receiveDataViaNetwork.receiveString();

            if (!confirmation.equalsIgnoreCase("yes")) {
                sendDataViaNetwork.sendStrings("Shutdown cancelled by admin.");
                return; // Si no es "yes", salimos.
            }
            // Si es "yes", NO hacemos return, seguimos abajo para pedir la contraseña
        }

        // 2. Pedir contraseña (Llegamos aquí si no hay clientes O si dijiste "yes")
        sendDataViaNetwork.sendStrings("PASSWORD_REQUIRED");

        String password = receiveDataViaNetwork.receiveString();

        // 3. Verificar contraseña
        if (Server.SHUTDOWN_PASSWORD.equals(password)) {
            sendDataViaNetwork.sendStrings("SHUTDOWN_OK");
            System.out.println("Password correct. Stopping server...");

            try { Thread.sleep(500); } catch (InterruptedException e) {}

            Server.stopServer();
            System.exit(0);
        } else {
            sendDataViaNetwork.sendStrings("WRONG_PASSWORD");
            System.out.println("Shutdown failed: Wrong password.");
        }
    }



    private static void patientReceiveAndSaveSignal(Patient patient, ReceiveDataViaNetwork receiveData, SendDataViaNetwork sendData) {
        ConnectionManager conMan = null;
        try {
            Signal signal = receiveData.receiveSignal();

            if (signal != null) {
                signal.setClientId(patient.getId());

                // --- YA NO GUARDAMOS ARCHIVO FÍSICO NI GENERAMOS GRÁFICA EN SERVIDOR ---
                // String generatedFileName = saveSignalToFile(signal, patient); // BORRAR
                // generateSignalGraph(signal, generatedFileName); // BORRAR OPCIONAL

                conMan = new ConnectionManager();
                JDBCSignalManager signalManager = new JDBCSignalManager(conMan);

                java.sql.Date date = new java.sql.Date(System.currentTimeMillis());

                // Pasamos "DB" como nombre de archivo porque ya no importa, los datos van dentro
                signalManager.addSignal(signal, "STORED_IN_DB", date);

                System.out.println("Signal saved in DB successfully.");
                sendData.sendStrings("OK");

            } else {
                sendData.sendStrings("ERROR");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conMan != null) conMan.close();
        }
    }

    private void doctorViewSignals() throws IOException {
        ConnectionManager cm = null;
        try {
            // 1. Recibir ID del paciente
            int patientIdForSignal = receiveDataViaNetwork.receiveInt();

            // 2. Preparar conexión y Manager
            cm = new ConnectionManager();
            JDBCSignalManager signalManager = new JDBCSignalManager(cm);

            // 3. Obtener lista de señales
            List<Signal> signals = signalManager.listSignalsByPatientId(patientIdForSignal);

            // 4. Enviar lista al doctor (Metadatos)
            sendDataViaNetwork.sendSignalList(signals);

            // Si no hay señales, cerramos y salimos
            if (signals.isEmpty()) {
                System.out.println("No signals found for patient " + patientIdForSignal);
                return;
            }

            // 5. Esperar selección del doctor
            int selectedSignalId = receiveDataViaNetwork.receiveInt();

            if (selectedSignalId == -1) {
                System.out.println("Doctor cancelled signal view.");
                return;
            }

            System.out.println("Retrieving content for Signal ID: " + selectedSignalId);

            // 6. Recuperar señal completa (Leyendo el archivo .txt)
            Signal fullSignal = signalManager.getSignalWithValues(selectedSignalId);

            if (fullSignal != null) {
                // 7. Enviar señal completa (Datos)
                sendDataViaNetwork.sendSignal(fullSignal);
                System.out.println("Signal data sent successfully.");
            } else {
                System.out.println("Error: Signal file not found or corrupted.");
                // Opcional: Podrías enviar una señal vacía o manejar error en el cliente
            }

        } catch (Exception e) {
            System.err.println("Error handling signal view: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Aseguramos cerrar la conexión temporal
            if (cm != null) {
                cm.close();
            }
        }
    }

}
