package jdbc;

import interfaces.PatientManager;
import pojos.MedicalInformation;
import pojos.Patient;
import pojos.User;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JDBCPatientManager implements PatientManager {

    private Connection c;
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
            String sql = "SELECT * FROM patient";
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

    @Override
    public Patient getPatientbyId(int id) { // para cuando el doctor escoge un paciente de la lista
        String query = "SELECT * FROM patient WHERE id = ?;";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Patient p = null;

        try {
            stmt = c.prepareStatement(query);
            stmt.setInt(1, id);

            rs = stmt.executeQuery();

            if (rs.next()) {
                int patientId = rs.getInt("id");
                String name = rs.getString("name");
                String surname = rs.getString("surname");
                String dni = rs.getString("dni");
                String dobString = rs.getString("dob");
                long millis = Long.parseLong(dobString);
                Date dob = new Date(millis);
                String sex = rs.getString("sex");
                int phone = rs.getInt("phone");
                String email = rs.getString("email");
                int insurance = rs.getInt("insurance");

                p = new Patient(patientId, name, surname, dni, dob, sex, phone, email, insurance);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return p;
    }





    @Override
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

    public boolean updatePatientName(int patientId, String newName) {
        if (newName.isEmpty()){
            return false;
        }

        try{
            String query = "UPDATE patient SET name = ? WHERE id = ?";
            PreparedStatement pstmt;
            pstmt = c.prepareStatement(query);
            pstmt.setString(1,newName);
            pstmt.setInt(2,patientId);
            pstmt.executeUpdate();
            pstmt.close();
            return true;
        }
        catch (SQLException e){
            System.out.println("Error updating patient");
            e.printStackTrace();
            return false;
        }


    }
    public boolean updatePatientSurname(int patientId, String newSurname) {
        if (newSurname.isEmpty()){
            return false;
        }

        try{
            String query = "UPDATE patient SET surname = ? WHERE id = ?";
            PreparedStatement pstmt;
            pstmt = c.prepareStatement(query);
            pstmt.setString(1,newSurname);
            pstmt.setInt(2,patientId);
            pstmt.executeUpdate();
            pstmt.close();
            return true;
        }
        catch (SQLException e){
            System.out.println("Error updating patient");
            e.printStackTrace();
            return false;
        }


    }
    public boolean updatePatientDNI(int patientId, String newDni) {
        if (newDni.isEmpty()){
            return false;
        }

        try{
            String query = "UPDATE patient SET dni = ? WHERE id = ?";
            PreparedStatement pstmt;
            pstmt = c.prepareStatement(query);
            pstmt.setString(1,newDni);
            pstmt.setInt(2,patientId);
            pstmt.executeUpdate();
            pstmt.close();
            return true;
        }
        catch (SQLException e){
            System.out.println("Error updating patient");
            e.printStackTrace();
            return false;
        }


    }

    public boolean updatePatientDob(int patientId, Date newDob) {
        if (newDob.equals(null)){
            return false;
        }

        try{
            String query = "UPDATE patient SET dob = ? WHERE id = ?";
            PreparedStatement pstmt;
            pstmt = c.prepareStatement(query);
            pstmt.setDate(1,newDob);
            pstmt.setInt(2,patientId);
            pstmt.executeUpdate();
            pstmt.close();
            return true;
        }
        catch (SQLException e){
            System.out.println("Error updating patient");
            e.printStackTrace();
            return false;
        }


    }
    public boolean updatePatientPhone(int id, int newPatientPhone) {
        if (newPatientPhone == 0){
            return false;
        }

        try{
            String query = "UPDATE patient SET phone = ? WHERE id = ?";
            PreparedStatement pstmt;
            pstmt = c.prepareStatement(query);
            pstmt.setInt(1,newPatientPhone);
            pstmt.setInt(2,id);
            pstmt.executeUpdate();
            pstmt.close();
            return true;
        }
        catch (SQLException e){
            System.out.println("Error updating patient");
            e.printStackTrace();
            return false;
        }


    }

    public boolean updatePatientEmail(int patientId, String newEmail) {
        if (newEmail.isEmpty()){
            return false;
        }

        try{
            String query = "UPDATE patient SET email = ? WHERE id = ?";
            PreparedStatement pstmt;
            pstmt = c.prepareStatement(query);
            pstmt.setString(1,newEmail);
            pstmt.setInt(2,patientId);
            pstmt.executeUpdate();
            pstmt.close();
            return true;
        }
        catch (SQLException e){
            System.out.println("Error updating patient");
            e.printStackTrace();
            return false;
        }


    }

    public boolean updatePatientSex(int patientId, String newSex) {
        if (newSex.isEmpty()){
            return false;
        }

        try{
            String query = "UPDATE patient SET sex = ? WHERE patientId = ?";
            PreparedStatement pstmt;
            pstmt = c.prepareStatement(query);
            pstmt.setString(1,newSex);
            pstmt.setInt(2,patientId);
            pstmt.executeUpdate();
            pstmt.close();
            return true;
        }
        catch (SQLException e){
            System.out.println("Error updating patient");
            e.printStackTrace();
            return false;
        }


    }
    public boolean updatePatientInsurance(int patientId, int newInsurance){
        if (newInsurance == 0){
            return false;
        }

        try{
            String query = "UPDATE patient SET insurance= ? WHERE id = ?";
            PreparedStatement pstmt;
            pstmt = c.prepareStatement(query);
            pstmt.setInt(1,newInsurance);
            pstmt.setInt(2,patientId);
            pstmt.executeUpdate();
            pstmt.close();
            return true;
        }
        catch (SQLException e){
            System.out.println("Error updating patient");
            e.printStackTrace();
            return false;
        }


    }



    @Override
    public int getPatientIDFromEmail(String email){
        String query = "SELECT id FROM patient WHERE email = ?;";
        PreparedStatement s = null;
        ResultSet rs = null;
        Integer id = null;
        try {
            s = c.prepareStatement(query);
            s.setString(1, email);
            rs = s.executeQuery();
            if (rs.next()) { // Move the cursor to the first row
                id = rs.getInt("id");
            }
            return id;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (s != null) s.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return id;

    }



}

