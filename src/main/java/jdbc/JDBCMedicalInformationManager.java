package jdbc;

import interfaces.MedicalInformationManager;
import pojos.MedicalInformation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
            String template = "INSERT INTO medicalInformation ...";
            PreparedStatement pstmt;
            pstmt = c.prepareStatement(template);

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
    public MedicalInformation getMedicalInformation(MedicalInformation m) {
        return null;
    }
}
