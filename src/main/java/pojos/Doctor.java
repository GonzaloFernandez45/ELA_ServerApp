package pojos;

import java.util.Date;
import java.util.Objects;

public class Doctor {
    private Integer id;
    private String name;
    private String surname;
    private Integer DNI;
    private Date birthDate;
    private String gender;
    public String email;

    public Doctor(int id, String name, String surname, int DNI, Date birthDate, String gender, String email) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.DNI = DNI;
        this.birthDate = birthDate;
        this.gender = gender;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public int getDNI() {
        return DNI;
    }

    public void setDNI(int DNI) {
        this.DNI = DNI;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Doctor doctor = (Doctor) o;
        return id == doctor.id && DNI == doctor.DNI && Objects.equals(name, doctor.name) && Objects.equals(surname, doctor.surname) && Objects.equals(birthDate, doctor.birthDate) && Objects.equals(gender, doctor.gender) && Objects.equals(email, doctor.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, surname, DNI, birthDate, gender, email);
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", DNI=" + DNI +
                ", birthDate=" + birthDate +
                ", gender='" + gender + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

}
