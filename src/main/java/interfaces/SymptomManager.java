package interfaces;

import pojos.Symptom;

import java.util.List;

public interface SymptomManager {
    public void addSymptom(Symptom s);
    public List<Symptom> listSymptoms();
    public Symptom getSymptom(Symptom s);
    public  Symptom getSymptomById(int symptomId);
    public List<Symptom> getSymptomsForMedicalInfo(int medicalInfoId) ;
    public List<Symptom> getSymptomsOfMedicalInformation(int medicalInformationId);




}
