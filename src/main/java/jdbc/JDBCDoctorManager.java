package jdbc;

import interfaces.DoctorManager;
import pojos.Doctor;

import java.sql.Connection;

public class JDBCDoctorManager implements DoctorManager {
    private static Connection c;
    private ConnectionManager conMan;
    public JDBCDoctorManager(ConnectionManager conMan) {
        this.conMan = conMan;

    }


    @Override
    public void addDoctor(Doctor d) {

    }
}
