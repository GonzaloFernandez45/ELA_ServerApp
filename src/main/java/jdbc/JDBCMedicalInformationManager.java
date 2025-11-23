package jdbc;

import interfaces.MedicalInformationManager;
import pojos.MedicalInformation;
import pojos.Patient;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JDBCMedicalInformationManager implements MedicalInformationManager {
    private static Connection c;
    private ConnectionManager conMan;

    public JDBCMedicalInformationManager(ConnectionManager conMan) {
        this.conMan = new ConnectionManager();
        this.c= conMan.getConnection();
    }

// me los ha dado por defecto
    @Override
    public void insertMedicalInformation(MedicalInformation m) {
        try{
            String template = "INSERT INTO medicalInformation (reportDate, medication, feedback) VALUES (?, ?, ?)";
            PreparedStatement pstmt;
            pstmt = c.prepareStatement(template);
            pstmt.setDate(1, m.getReportDate());

            List<String> meds = m.getMedication();
            String medsAsString = String.join(",", meds);
            pstmt.setString(2, medsAsString);

            pstmt.setString(3, m.getFeedback());
            pstmt.executeUpdate();
            pstmt.close();
        }catch (SQLException e) {
            System.out.println("Error in the database");
            e.printStackTrace();
        }
    }

    @Override
    public void updateMedicalInformation(MedicalInformation m) {
        try {
            String sql = "UPDATE medicalInformation SET feedback = ? WHERE id = ?";
            PreparedStatement pstmt = c.prepareStatement(sql);

            pstmt.setString(1, m.getFeedback());  // Asumimos que el feedback ahora está presente
            pstmt.setInt(2, m.getId());  // Usamos el ID de la información médica para actualizar el registro específico

            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error updating medical information");
            e.printStackTrace();
        }
    }

    @Override
    public void deleteMedicalInformation(MedicalInformation m) {

    }

    @Override
    public MedicalInformation getMedicalInfoByPatientId(int patientId) {
        MedicalInformation m = null;

        try {
            String sql = "SELECT * FROM medicalInformation WHERE patient_id = ?";
            PreparedStatement ps = c.prepareStatement(sql);

            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                m = new MedicalInformation();
                m.setId(rs.getInt("id"));
                m.setPatient_id(rs.getInt("patient_id"));
                m.setFeedback(rs.getString("feedback"));
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.out.println("Error retrieving medical information for patient " + patientId);
            e.printStackTrace();
        }

        return m;
    }
}
