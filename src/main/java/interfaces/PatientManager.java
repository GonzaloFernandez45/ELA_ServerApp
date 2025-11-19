package interfaces;

import pojos.Patient;
import pojos.User;

import java.util.List;

public interface PatientManager {
    public void addPatient(Patient p);
    public List<Patient> listPatients();
    Patient getPatientbyId(int id);
    void updatePatient(Patient p);
    int getPatientIDFromEmail(String email);
    public Patient getPatientFromUser(User user);
}
