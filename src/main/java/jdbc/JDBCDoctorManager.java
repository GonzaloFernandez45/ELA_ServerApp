package jdbc;

import interfaces.DoctorManager;
import pojos.Doctor;
import pojos.Patient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class JDBCDoctorManager implements DoctorManager {
    private static Connection c;
    private ConnectionManager conMan;
    public JDBCDoctorManager(ConnectionManager conMan) {
        this.conMan = conMan;

    }


    @Override
    public void addDoctor(Doctor d) {
        try {
            String template = "INSERT INTO doctor (name, surname, dni, dob, sex,email) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt;
            pstmt = c.prepareStatement(template);
            pstmt.setString(1, d.getName());
            pstmt.setString(2, d.getSurname());
            pstmt.setString(3, d.getDNI());
            pstmt.setDate(4, d.getBirthDate());
            pstmt.setString(5, d.getSex());
            pstmt.setString(6, d.getEmail());
            pstmt.executeUpdate();
            pstmt.close();
        }catch (SQLException e) {
            System.out.println("Error in the database");
            e.printStackTrace();
        }

    }

    @Override
    public List<Doctor> listDoctors() {
        List<Doctor> doctors = new ArrayList<Doctor>();
        try {
            String sql = "SELECT * FROM doctor";
            PreparedStatement pstmt = c.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                Doctor doctor = new Doctor(rs.getInt("id"), rs.getString("name"), rs.getString("surname"), rs.getString("dni"), rs.getDate("birthDate"), rs.getString("sex"), rs.getString("email"));
                doctors.add(doctor);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error in the database");
            e.printStackTrace();
        }
        return doctors;
    }
}
