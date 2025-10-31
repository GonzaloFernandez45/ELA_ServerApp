package interfaces;

import pojos.Patient;

import java.util.List;

public interface PatientManager {
    public void addPatient(Patient p);
    public List<Patient> listPatients();


}
