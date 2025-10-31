package interfaces;

import pojos.Symptom;

import java.util.List;

public interface SymptomManager {
    public void addSymptom(Symptom s);
    public List<Symptom> listSymptoms();
    public Symptom getSymptom(Symptom s);

}
