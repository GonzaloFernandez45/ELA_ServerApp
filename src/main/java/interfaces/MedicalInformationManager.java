package interfaces;

import pojos.MedicalInformation;

import java.util.List;

public interface MedicalInformationManager {
    public void insertMedicalInformation(MedicalInformation m);
    public void updateMedicalInformation(MedicalInformation m);
    public void deleteMedicalInformation(MedicalInformation m);
    public List<MedicalInformation> getMedicalInfoByPatientId(int id);

}
