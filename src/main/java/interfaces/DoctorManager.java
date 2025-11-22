package interfaces;

import pojos.Doctor;

import java.util.List;

public interface DoctorManager {
    public void addDoctor(Doctor d);
    public List<Doctor> listDoctors();
    int getDoctorIDFromEmail(String email);
    Doctor getDoctorbyId(int id);
}
