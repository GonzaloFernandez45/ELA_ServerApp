package interfaces;

import pojos.MedicalInformation;

import java.util.List;

public interface MedicalInformationManager {
    public void insertMedicalInformation(MedicalInformation m);
    void updateMedicalInformation(int patient_id, String feedback);
    public void deleteMedicalInformation(MedicalInformation m);
    public List<MedicalInformation> getMedicalInfoByPatientId(int id);
    public boolean updateFeedback(int medicalInfoId, String feedback);

}
