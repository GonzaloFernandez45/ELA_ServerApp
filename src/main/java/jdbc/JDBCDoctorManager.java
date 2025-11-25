package jdbc;

import interfaces.DoctorManager;
import pojos.Doctor;
import pojos.Patient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JDBCDoctorManager implements DoctorManager {
    private Connection c;
    private ConnectionManager conMan;
    public JDBCDoctorManager(ConnectionManager conMan) {
        this.conMan = conMan;
        this.c = conMan.getConnection();

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

    public int getDoctorIDFromEmail(String email){
        String query = "SELECT id FROM doctor WHERE email = ?;";
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

    public Doctor getDoctorbyId(int id) { // para cuando el doctor escoge un paciente de la lista
        String query = "SELECT * FROM doctor WHERE id = ?;";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Doctor d = null;

        try {
            stmt = c.prepareStatement(query);
            stmt.setInt(1, id);

            rs = stmt.executeQuery();

            if (rs.next()) {
                int doctorId = rs.getInt("id");
                String name = rs.getString("name");
                String surname = rs.getString("surname");
                String dni = rs.getString("dni");
                String dobString = rs.getString("dob");
                long millis = Long.parseLong(dobString);
                Date dob = new Date(millis);
                String sex = rs.getString("sex");
                String email = rs.getString("email");


                d = new Doctor(doctorId, name, surname, dni, dob, sex,email);
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

        return d;
    }
}
