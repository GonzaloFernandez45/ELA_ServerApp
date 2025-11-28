package jdbc;

import interfaces.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionManager {
    private Connection conn;
    private DoctorManager docMan;
    private PatientManager pMan;
    private SymptomManager SympMan;
    private MedicalInformationManager MedMan;
    private UserManager uMan;

    public Connection getConnection() {
        return conn;
    }

    public ConnectionManager() {
        this.connect();
        this.docMan = new JDBCDoctorManager(this);
        this.pMan = new JDBCPatientManager(this);
        //this.SympMan = new JDBCSymptomManager(this);
        //this.MedMan = new JDBCMedicalInformationManager(this);
        //this.uMan = new JDBCUserManager(this);


        this.createTables();
        //this.insertSymptoms();

    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:./db/ELA_telemedicine.db");
            conn.createStatement().execute("PRAGMA foreign_keys=ON");
        } catch (ClassNotFoundException cnfE) {
            System.out.println("Databases ELA not loaded");
            cnfE.printStackTrace();
        } catch (SQLException sqlE) {
            System.out.println("Error with database");
            sqlE.printStackTrace();
        }
    }
    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            System.out.println("Error closing the database");
            e.printStackTrace();
        }
    }

    private void createTables() {
        try {
            Statement createTables1 = conn.createStatement();
            String create1 = "CREATE TABLE patient ("
                    + "id	INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name	TEXT NOT NULL,"
                    + "surname	TEXT NOT NULL,"
                    + "dni	TEXT NOT NULL,"
                    + "dob	TEXT,"
                    + "sex	TEXT,"
                    + "phone INTEGER,"
                    + "email	TEXT NOT NULL UNIQUE,"
                    + "insurance	INTEGER NOT NULL )";
            createTables1.executeUpdate(create1);
            createTables1.close();

            Statement createTables2 = conn.createStatement();
            String create2 = "CREATE TABLE symptom ("
                    + "id	INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "description TEXT NOT NULL)";
            createTables2.executeUpdate(create2);
            createTables2.close();

            Statement createTables3 = conn.createStatement();
            String create3 = "CREATE TABLE medical_information ("
                    + "id	INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "reportDate  TEXT NOT NULL,"
                    + "medication TEXT,"
                    + "feedback TEXT,"
                    + "patient_id INTEGER,"
                    + "FOREIGN KEY (patient_id) REFERENCES patient(id))";
            createTables3.executeUpdate(create3);
            createTables3.close();

            Statement createTables4 = conn.createStatement();
            String create4 = "CREATE TABLE doctor ("
                    + "id	INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name TEXT NOT NULL,"
                    + "surname TEXT NOT NULL,"
                    + "dni TEXT NOT NULL,"
                    + "dob TEXT NOT NULL,"
                    + "sex TEXT,"
                    + "email TEXT)";
            createTables4.executeUpdate(create4);
            createTables4.close();

            Statement createTables5 = conn.createStatement();
            String create5 = "CREATE TABLE symptom_medicalInformation ("
                    + "symptom_id INTEGER,"
                    + "MEDICAL_information_id INTEGER,"
                    + "FOREIGN KEY (symptom_id) REFERENCES symptom(id)),"
                    + "FOREIGN KEY (medical_information_id) REFERENCES medicalInformation(id)),"
                    + "PRIMARY KEY (symptom_id,medical_information_id))";
            createTables5.executeUpdate(create5);
            createTables5.close();

            Statement createTables6 = conn.createStatement();
            String create6 = "CREATE TABLE administrator("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "email TEXT NOT NULL UNIQUE,"
                    + "dni TEXT NOT NULL,";

            createTables6.executeUpdate(create6);
            createTables6.close();

            Statement createTables7 = conn.createStatement();
            String create7 = "CREATE TABLE user ("
                    + "user_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "email TEXT NOT NULL UNIQUE,"
                    + "password TEXT NOT NULL,"
                    + "role TEXT NOT NULL,"
                    + "patient_id INTEGER,"
                    + "doctor_id INTEGER,"
                    + "FOREIGN KEY (patient_id) REFERENCES patient(id)),"
                    + "FOREIGN KEY (doctor_id) REFERENCES doctor(id)),"
                    + "FOREIGN KEY (admin_id) REFERENCES administrator(id))";
            createTables7.executeUpdate(create7);
            createTables7.close();
            this.insertSymptoms();

        }catch (SQLException sqlE) {
            if (sqlE.getMessage().contains("already exist")){
                System.out.println("No need to create the tables; already there");
            }
            else {
                System.out.println("Error in query");
                sqlE.printStackTrace();
            }
        }
    }

    private void insertSymptoms(){
        try {
            Statement insertSymptom1 = conn.createStatement();
            String symptom1 = "INSERT INTO symptom VALUES(1, 'Muscle fatigue')";
            insertSymptom1.executeUpdate(symptom1);
            insertSymptom1.close();

            Statement insertSymptom2 = conn.createStatement();
            String symptom2 = "INSERT INTO symptom VALUES(2,'Difficulty moving limbs' )";
            insertSymptom2.executeUpdate(symptom2);
            insertSymptom2.close();

            Statement insertSymptom3 = conn.createStatement();
            String symptom3 = "INSERT INTO symptom VALUES(3,'Difficulty speaking')";
            insertSymptom3.executeUpdate(symptom3);
            insertSymptom3.close();

            Statement insertSymptom4 = conn.createStatement();
            String symptom4 = "INSERT INTO symptom VALUES(4,'Difficulty swallowing')";
            insertSymptom4.executeUpdate(symptom4);
            insertSymptom4.close();

            Statement insertSymptom5 = conn.createStatement();
            String symptom5 = "INSERT INTO symptom VALUES(5,'Muscle spasms or cramps')";
            insertSymptom5.executeUpdate(symptom5);
            insertSymptom5.close();

            Statement insertSymptom6 = conn.createStatement();
            String symptom6 = "INSERT INTO symptom VALUES(6,'Shortness of breath')";
            insertSymptom6.executeUpdate(symptom6);
            insertSymptom6.close();

            Statement insertSymptom7 = conn.createStatement();
            String symptom7 = "INSERT INTO symptom VALUES(7,'Weak neck muscles')";
            insertSymptom7.executeUpdate(symptom7);
            insertSymptom7.close();

            Statement createTables8 = conn.createStatement();
            String create8 = "CREATE TABLE signal ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "patient_id INTEGER,"
                    + "type TEXT,"
                    + "record_date TEXT,"
                    + "filename TEXT,"
                    + "FOREIGN KEY (patient_id) REFERENCES patient(id))";
            createTables8.executeUpdate(create8);
            createTables8.close();

        }catch (SQLException sqlE) {
            if (sqlE.getMessage().contains("UNIQUE constraint failed")) {
                System.out.println("No symptom to insert the needs; already there");
            } else {
                System.out.println("Error in query");
                sqlE.printStackTrace();
            }
        }

    }

}
