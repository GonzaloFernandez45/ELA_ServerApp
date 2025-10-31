package jdbc;

import interfaces.PatientManager;
import pojos.Patient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JDBCPatientManager implements PatientManager {

    private static Connection c;
    private ConnectionManager conMan;

    public JDBCPatientManager(ConnectionManager conMan) {
        this.conMan = conMan;
        this.c = conMan.getConnection();
    }

    @Override
    public void addPatient(Patient p) {
        try {
            String template = "INSERT INTO patient (name, surname, dni, dob, sex, phone, email, insurance) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt;
            pstmt = c.prepareStatement(template);
            pstmt.setString(1, p.getName());
            pstmt.setString(2, p.getSurname());
            pstmt.setString(3, p.getDni());
            pstmt.setDate(4, p.getDateOfBirth());
            pstmt.setString(5, p.getSex());
            pstmt.setInt(6, p.getPhone());
            pstmt.setString(7, p.getEmail());
            pstmt.setInt(8, p.getInsurance());
            pstmt.executeUpdate();
            pstmt.close();
        }catch (SQLException e) {
            System.out.println("Error in the database");
            e.printStackTrace();
        }

    }

    @Override
    public List<Patient> listPatients() {
        List<Patient> patients = new ArrayList<Patient>();
        try {
            String sql = "SELECT name, surname, insurance FROM patient";
            PreparedStatement pstmt = c.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                Patient patient = new Patient(rs.getInt("id"), rs.getString("name"), rs.getString("surname"), rs.getInt("insurance"));
                patients.add(patient);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error in the database");
            e.printStackTrace();
        }
        return patients;
    }
}

