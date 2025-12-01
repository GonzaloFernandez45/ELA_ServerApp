package interfaces;

import pojos.MedicalInformation;
import pojos.Symptom;

import java.sql.Date;
import java.util.List;

/**
 * Interface for managing the medical information associated with patients.
 *
 * Implementations will handle database operations (insert, update, delete, queries).
 */
public interface MedicalInformationManager {
    /**
     * Inserts a new MedicalInformation record in the data source.
     *
     *
     * @param m MedicalInformation object to be stored.
     */
    public void insertMedicalInformation(MedicalInformation m);

    /**
     * Updates the feedback text of the latest (or a specific) MedicalInformation
     * related to a given patient.
     *
     *
     * @param patient_id ID of the patient whose medical info we want to update.
     * @param feedback   new feedback text to store.
     */
    void updateMedicalInformation(int patient_id, String feedback);

    /**
     * Deletes a MedicalInformation record from the data source.
     *
     * @param m MedicalInformation entity to delete.
     */
    public void deleteMedicalInformation(MedicalInformation m);

    /**
     * Returns all MedicalInformation records belonging to a given patient.
     *
     * @param id patient ID (foreign key in MedicalInformation).
     * @return list of MedicalInformation objects for that patient.
     */
    public List<MedicalInformation> getMedicalInfoByPatientId(int id);

    /**
     * Updates the feedback field of a specific MedicalInformation record.
     *
     * @param medicalInfoId ID of the MedicalInformation record to update.
     * @param feedback      new feedback text.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateFeedback(int medicalInfoId, String feedback);

    /**
     * Retrieves a MedicalInformation record by date and patient.
     *
     *
     * @param date       date of the medical record (java.sql.Date).
     * @param patient_id ID of the patient.
     * @return MedicalInformation for that patient and date, or null if not found.
     */
    public MedicalInformation getMedicalInformationByDate(Date date, int patient_id);

    /**
     * Links a Symptom to an existing MedicalInformation record.
     *
     * In the DB, inserts a row into a join table
     * (symptom_medicalInformation) with:
     * - medicalInformationId
     * - symptomId (from Symptom object)
     *
     * @param medicalInformationId ID of the MedicalInformation record.
     * @param symptom              Symptom object to associate with that record.
     */
    public void insertSymptomMedicalInformation(int medicalInformationId, Symptom symptom);


}
