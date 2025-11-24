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
            String template = "INSERT INTO medicalInformation (symptoms, reportDate, medication) VALUES (?, ?, ?)";
            PreparedStatement pstmt;
            pstmt = c.prepareStatement(template);
            pstmt.setString(1, String.valueOf(m.getSymptoms()));
            pstmt.setString(2, String.valueOf(m.getReportDate()));
            pstmt.setString(3, String.valueOf(m.getMedication()));
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
    public List<MedicalInformation> getMedicalInfoByPatientId(int patientId) {
        List<MedicalInformation> medicalInfoList = new ArrayList<>();

        String sql = "SELECT * FROM medical_information WHERE patient_id = ? ORDER BY reportDate DESC"; // Ordenamos por fecha

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                MedicalInformation m = new MedicalInformation();
                m.setId(rs.getInt("id"));
                m.setPatient_id(rs.getInt("patient_id"));
                m.setFeedback(rs.getString("feedback"));
                m.setReportDate(rs.getDate("reportDate"));
                // Asumiendo que tienes otros atributos como symptoms y medication, agrégales aquí

                medicalInfoList.add(m);
            }

            rs.close();

        } catch (SQLException e) {
            System.out.println("Error retrieving medical information for patient " + patientId);
            e.printStackTrace();
        }

        return medicalInfoList;
    }
    @Override

    public boolean updateFeedback(int medicalInfoId, String feedback) {
        String sql = "UPDATE medical_information SET feedback = ? WHERE id = ?";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, feedback);
            ps.setInt(2, medicalInfoId);  // Usamos el id de medical_information para actualizar un único registro

            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0; // Si se actualizó al menos un registro, es éxito

        } catch (SQLException e) {
            System.out.println("Error updating feedback for medical information ID " + medicalInfoId);
            e.printStackTrace();
            return false;
        }
    }


}
