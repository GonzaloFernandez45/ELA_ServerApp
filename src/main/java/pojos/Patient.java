
package pojos;

import java.sql.Date;
import java.util.Objects;

public class Patient {
    private Integer id;
    private String name;
    private String surname;
    private String dni;
    private java.sql.Date dateOfBirth;
    private String sex;
    private Integer phone;
    private String email;
    private Integer insurance;

    public Patient(Integer id, String surname, String name, Integer insurance) {
        this.surname = surname;
        this.name = name;
        this.id = id;
        this.insurance = insurance;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(java.sql.Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Integer getPhone() {
        return phone;
    }

    public void setPhone(Integer phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getInsurance() {
        return insurance;
    }

    public void setInsurance(Integer insurance) {
        this.insurance = insurance;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return Objects.equals(id, patient.id) && Objects.equals(name, patient.name) && Objects.equals(surname, patient.surname) && Objects.equals(dni, patient.dni) && Objects.equals(dateOfBirth, patient.dateOfBirth) && Objects.equals(sex, patient.sex) && Objects.equals(phone, patient.phone) && Objects.equals(email, patient.email) && Objects.equals(insurance, patient.insurance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, surname, dni, dateOfBirth, sex, phone, email, insurance);
    }

    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", dni='" + dni + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", sex='" + sex + '\'' +
                ", phone=" + phone +
                ", email='" + email + '\'' +
                ", insurance=" + insurance +
                '}';
    }
}
