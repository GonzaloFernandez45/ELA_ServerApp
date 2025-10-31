package interfaces;

import pojos.MedicalInformation;

public interface MedicalInformationManager {
    public void insertMedicalInformation(MedicalInformation m);
    public void updateMedicalInformation(MedicalInformation m);
    public void deleteMedicalInformation(MedicalInformation m);
    public MedicalInformation getMedicalInformation(MedicalInformation m);

}
