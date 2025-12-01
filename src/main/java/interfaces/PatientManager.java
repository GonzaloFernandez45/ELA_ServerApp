package interfaces;

import pojos.Patient;
import pojos.User;

import java.util.List;

/**
 * Manages CRUD operations for Patient entities.
 * Implementations handle DB access: insert, update, lookup by ID/email.
 */
public interface PatientManager {

    /**
     * Inserts a new patient into the data source.
     *
     * @param p patient to insert.
     */
    public void addPatient(Patient p);

    /**
     * Returns all patients stored in the system.
     *
     * @return list of patients.
     */
    public List<Patient> listPatients();

    /**
     * Retrieves a patient by ID.
     *
     * @param id patient ID.
     * @return patient, or null if not found.
     */
    Patient getPatientbyId(int id);

    /**
     * Updates an existing patient.
     *
     * @param p patient with updated data.
     */
    void updatePatient(Patient p);

    /**
     * Retrieves the patient ID from email.
     *
     * @param email patient email.
     * @return patient ID, or -1/impl-specific if not found.
     */
    int getPatientIDFromEmail(String email);

}
