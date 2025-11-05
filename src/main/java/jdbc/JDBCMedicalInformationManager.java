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

    }

    @Override
    public void deleteMedicalInformation(MedicalInformation m) {

    }

    @Override
    public MedicalInformation getMedicalInformation(MedicalInformation m) {
        return null;
    }
}
