package interfaces;

import pojos.Symptom;

import java.util.List;

/**
 * Manages symptom entities and their association with medical records.
 * Implementations handle DB storage, lookup, and linking symptoms to medical info.
 */
public interface SymptomManager {

    /**
     * Inserts a new symptom into the system.
     *
     * @param s symptom to insert.
     */
    public void addSymptom(Symptom s);

    /**
     * Returns all available symptoms.
     *
     * @return list of symptoms.
     */
    public List<Symptom> listSymptoms();

    /**
     * Retrieves a symptom matching the given Symptom object
     * (typically checking by name).
     *
     * @param s symptom reference.
     * @return matching symptom or null if not found.
     */
    public Symptom getSymptom(Symptom s);

    /**
     * Retrieves a symptom by its unique ID.
     *
     * @param symptomId ID of the symptom.
     * @return symptom or null if not found.
     */
    public  Symptom getSymptomById(int symptomId);

    /**
     * Returns all symptoms linked to a given MedicalInformation record.
     *
     * @param medicalInformationId ID of the medical record.
     * @return list of symptoms associated with it.
     */
    public List<Symptom> getSymptomsOfMedicalInformation(int medicalInformationId);




}
