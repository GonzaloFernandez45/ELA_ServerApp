package jdbc;

import interfaces.MedicalInformationManager;
import pojos.MedicalInformation;

import java.sql.Connection;

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
