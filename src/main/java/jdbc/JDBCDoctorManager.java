package jdbc;

import interfaces.DoctorManager;
import pojos.Doctor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

public class JDBCDoctorManager implements DoctorManager {
    private static Connection c;
    private ConnectionManager conMan;
    public JDBCDoctorManager(ConnectionManager conMan) {
        this.conMan = conMan;

    }


    @Override
    public void addDoctor(Doctor d) {
        try {
            String template = "INSERT INTO doctor (name, surname, dni, dob, gender,email) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt;
            pstmt = c.prepareStatement(template);
            pstmt.setString(1, d.getName());
            pstmt.setString(2, d.getSurname());
            pstmt.setInt(3, d.getDNI());
            pstmt.setDate(4, d.getBirthDate());
            pstmt.setString(5, d.getGender());
            pstmt.setString(6, d.getEmail());
            pstmt.executeUpdate();
            pstmt.close();
        }catch (SQLException e) {
            System.out.println("Error in the database");
            e.printStackTrace();
        }

    }
}
