package pojos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Symptom {

    private Integer id;
    private String description;
    private List<MedicalInformation> medicalInformations;

    public Symptom(String description) {
        this.id = id;
        this.description = description;
        this.medicalInformations = new ArrayList<MedicalInformation>();
    }

    public Symptom(Integer id, String description, List<MedicalInformation> medicalInformations) {
        this.id = id;
        this.description = description;
        this.medicalInformations = new ArrayList<MedicalInformation>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<MedicalInformation> getMedicalInformations() {
        return medicalInformations;
    }

    public void setMedicalInformations(List<MedicalInformation> medicalInformations) {
        this.medicalInformations = medicalInformations;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Symptom symptoms = (Symptom) o;
        return Objects.equals(id, symptoms.id) && Objects.equals(description, symptoms.description) && Objects.equals(medicalInformations, symptoms.medicalInformations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description, medicalInformations);
    }

    @Override
    public String toString() {
        return "Symptoms{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", medicalInformations=" + medicalInformations +
                '}';
    }
}
