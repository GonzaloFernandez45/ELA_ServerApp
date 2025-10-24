package pojos;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MedicalInformation {
    private Integer id;
    private List<Symptom> symptoms;
    private Date reportDate;
    private List<String> medication;


    public MedicalInformation(Integer id, List<Symptom> symptoms, Date reportDate, List<String> medication) {
        this.id = id;
        this.symptoms = symptoms;
        this.reportDate = reportDate;
        this.medication = medication;
    }
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public List<Symptom> getSymptoms() {
        return symptoms;
    }
    public void setSymptoms(List<Symptom> symptoms) {
        this.symptoms = symptoms;
    }
    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;

    }
    public List<String> getMedication() {
        return medication;
    }
    public void setMedication(List<String> medication) {
        this.medication = medication;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MedicalInformation that = (MedicalInformation) o;
        return Objects.equals(id, that.id) && Objects.equals(symptoms, that.symptoms) && Objects.equals(reportDate, that.reportDate) && Objects.equals(medication, that.medication);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, symptoms, reportDate, medication);
    }

    @Override
    public String toString() {
        return "MedicalInformation{" +
                "id=" + id +
                ", symptoms=" + symptoms +
                ", reportDate=" + reportDate +
                ", medication=" + medication +
                '}';
    }
}
