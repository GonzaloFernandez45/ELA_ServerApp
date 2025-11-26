package interfaces;

import pojos.MedicalInformation;
import pojos.Symptom;

import java.sql.Date;
import java.util.List;

public interface MedicalInformationManager {
    public void insertMedicalInformation(MedicalInformation m);
    void updateMedicalInformation(int patient_id, String feedback);
    public void deleteMedicalInformation(MedicalInformation m);
    public List<MedicalInformation> getMedicalInfoByPatientId(int id);
    public boolean updateFeedback(int medicalInfoId, String feedback);
    public MedicalInformation getMedicalInformationByDate(Date date, int patient_id);
    public void insertSymptomMedicalInformation(int medicalInformationId, Symptom symptom);

}
