package jdbc;

import interfaces.PatientManager;
import pojos.Patient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Statement;

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

    public Patient getPatientbyId( int id) {//para cuando el doctor escoge una paciente de la lista
        try {
            String sql = "SELECT * FROM patient WHERE id = " + id;
            Statement stmt;
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            Patient p = null;
            while(rs.next()){
                p= new Patient(
                        rs.getInt("id"),
                        rs.getString("surname"),
                        rs.getString("name"),
                        rs.getInt("insurance") );
            }
            return p;
        }catch(SQLException e) {
            System.out.println("Error in the database");
            e.printStackTrace();
        }
        return null;

    }

    public void updatePatient(Patient p) {//la opcion del doctor de "modify patient data"
        try{
            String sql= " UPDATE patient SET name =?, surname=?, dob=?,sex=?,phone=?,email=?,insurance=? WHERE id=?";
            PreparedStatement pstmt;
            pstmt= c.prepareStatement(sql);
            pstmt.setString(1,p.getName());
            pstmt.setString(2,p.getSurname());
            pstmt.setDate(3, p.getDateOfBirth());
            pstmt.setString(4,p.getSex());
            pstmt.setInt(5,p.getPhone());
            pstmt.setString(6,p.getEmail());
            pstmt.setInt(7,p.getInsurance());
            pstmt.executeUpdate();
            pstmt.close();

        }catch (SQLException e){
            System.out.println("Error updating patient");
            e.printStackTrace();
        }



    }


}

