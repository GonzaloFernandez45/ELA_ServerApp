package jdbc;

import interfaces.MedicalInformationManager;
import pojos.MedicalInformation;
import pojos.Patient;
import pojos.Symptom;

import java.sql.*;
import java.util.ArrayList;
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
            String template = "INSERT INTO medical_information (reportDate, medication, feedback, patient_id) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt;
            pstmt = c.prepareStatement(template);
            pstmt.setDate(1, m.getReportDate());

            List<String> meds = m.getMedication();
            String medsAsString = String.join(",", meds);
            pstmt.setString(2, medsAsString);

            pstmt.setString(3, m.getFeedback());
            pstmt.setInt(4, m.getPatient_id());
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
        List<MedicalInformation> medicalInformationList = new ArrayList<>();
        JDBCSymptomManager symptomManager = new JDBCSymptomManager(conMan);  // Instanciamos JDBCSymptomManager


        try {
            // Primer paso: Recuperar la información médica
            String sql = "SELECT * FROM medical_information WHERE patient_id = ?";
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                MedicalInformation m = new MedicalInformation();
                m.setId(rs.getInt("id"));
                String reportDateString = rs.getString("reportDate");
                long millis = Long.parseLong(reportDateString);
                Date reportDate = new Date(millis);
                m.setReportDate(reportDate);

                // Recuperar la lista de medicamentos
                String medsString = rs.getString("medication");
                List<String> medication = (medsString == null || medsString.isEmpty())
                        ? List.of()
                        : List.of(medsString.split(","));
                m.setMedication(medication);

                // Recuperar el feedback
                m.setFeedback(rs.getString("feedback"));

                // Segundo paso: Recuperar los síntomas asociados a la información médica
                List<Symptom> symptoms = symptomManager.getSymptomsForMedicalInfo(m.getId());  // Aquí usamos el método para obtener los síntomas
                m.setSymptoms(symptoms);  // Establecer la lista de síntomas en la información médica

                // Añadir la entrada de información médica a la lista
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

    public MedicalInformation getMedicalInformationByDate(Date date, int patient_id){
        String query = "SELECT * FROM medical_information WHERE reportDate = ? AND patient_id = ?;";
        MedicalInformation medicalInformation = new MedicalInformation();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = c.prepareStatement(query);
            stmt.setDate(1, date);
            stmt.setInt(2, patient_id);

            rs = stmt.executeQuery();

            if (rs.next()) {
                int reportId = rs.getInt("id");
                medicalInformation.setId(reportId);
                String dateString = rs.getString("reportDate");
                long millis = Long.parseLong(dateString);
                Date reportDate = new Date(millis);
                medicalInformation.setReportDate(reportDate);
                String feedback = rs.getString("feedback");
                medicalInformation.setFeedback(feedback);
                int patientId = rs.getInt("patient_id");
                medicalInformation.setPatient_id(patientId);
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

        return medicalInformation;
    }

    public void insertSymptomMedicalInformation(int medicalInformationId, Symptom symptom){
        try{
            String template = "INSERT INTO symptom_medicalInformation (symptom_id, medical_information_id) VALUES (?, ?)";
            PreparedStatement pstmt;
            pstmt = c.prepareStatement(template);
            pstmt.setInt(1, symptom.getId());
            pstmt.setInt(2, medicalInformationId);

            pstmt.executeUpdate();
            pstmt.close();
        }catch (SQLException e) {
            System.out.println("Error in the database");
            e.printStackTrace();
        }
    }

//    @Override
//    public List<MedicalInformation> getMedicalInformationByPatientId(int patientId) {
//        List<MedicalInformation> medicalInfos = new ArrayList<>();
//        String query = "SELECT * FROM medical_information WHERE patient_id = ?";
//        PreparedStatement s = null;
//        ResultSet rs = null;
//
//        try {
//            s = c.prepareStatement(query);
//            s.setInt(1, patientId);
//            rs = s.executeQuery();
//
//            while (rs.next()) {
//                // Aquí debes asegurarte de obtener los campos correctos y crear el objeto MedicalInformation
//                MedicalInformation medicalInfo = new MedicalInformation();
//                medicalInfo.setId(rs.getInt("id"));
//                medicalInfo.setReportDate(rs.getDate("report_date"));
//                //medicalInfo.setSymptoms(rs.getString("symptoms"));
//                //medicalInfo.setMedication(rs.getString("medication"));
//                medicalInfo.setFeedback(rs.getString("feedback"));
//
//                medicalInfos.add(medicalInfo);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (rs != null) rs.close();
//                if (s != null) s.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//
//        return medicalInfos;
//    }

}
