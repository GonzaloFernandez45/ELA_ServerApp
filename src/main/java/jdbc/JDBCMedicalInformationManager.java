package jdbc;

import interfaces.MedicalInformationManager;
import pojos.MedicalInformation;

import java.sql.*;
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
    public void updateMedicalInformation(int patient_id, String feedback) {
        try {
            String sql = "UPDATE medicalInformation SET feedback = ? WHERE patient_id = ?";
            PreparedStatement pstmt = c.prepareStatement(sql);

            pstmt.setString(1, feedback);  // Asumimos que el feedback ahora está presente
            pstmt.setInt(2, patient_id);  //

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
        List<MedicalInformation> medicalInformationList = null;

        try {
            String sql = "SELECT * FROM medicalInformation WHERE patient_id = ?";
            PreparedStatement ps = c.prepareStatement(sql);

            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                MedicalInformation m = new MedicalInformation();
                //EL ID
                m.setId(rs.getInt("id"));
                //El date, se guarda en milisegundos en la db
                String reportDateString = rs.getString("reportDate");
                long millis = Long.parseLong(reportDateString);
                Date reportDate = new Date(millis);
                m.setReportDate(reportDate);
                //Para sacar una lista a partir de un string con delimitadores
                String medsString = rs.getString("medication");
                List<String> medication = (medsString == null || medsString.isEmpty())
                        ? List.of()
                        : List.of(medsString.split(","));
                m.setMedication(medication);
                //set el feedback
                m.setFeedback(rs.getString("feedback"));
                //lo vamos añadiendo a una lista de medical infos
                medicalInformationList.add(m);
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.out.println("Error retrieving medical information for patient " + patientId);
            e.printStackTrace();
        }

        return medicalInformationList;
    }

}
