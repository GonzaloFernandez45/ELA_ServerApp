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

    public Connection getConnection() {
        return conn;
    }

    public ConnectionManager() {
        this.connect();
        this.docMan = new JDBCDoctorManager(this);
        this.pMan = new JDBCPatientManager(this);

        this.createTables();
        //this.insertSymptom();

    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:./db/ELA_telemedicine.db");
            conn.createStatement().execute("PRAGMA foreign_keys=ON");
        } catch (ClassNotFoundException cnfE) {
            System.out.println("Databases prosthetic not loaded");
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
                    + "email	TEXT,"
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
                    + "medication TEXT)";
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
                    + "FOREIGN KEY (symptom_id) REFERENCES symptom(ID)),"
                    + "FOREIGN KEY (medical_information_id) REFERENCES medicalInformation(ID)),"
                    + "PRIMARY KEY (symptom_id,medical_information_id))";
            createTables5.executeUpdate(create5);
            createTables5.close();

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
            String symptom1 = "INSERT INTO symptom VALUES(1, 'muscle fatigue')";
            insertSymptom1.executeUpdate(symptom1);
            insertSymptom1.close();

            Statement insertSymptom2 = conn.createStatement();
            String symptom2 = "INSERT INTO symptom VALUES(1, 'Difficulty moving limbs' )";
            insertSymptom1.executeUpdate(symptom1);
            insertSymptom1.close();

            Statement insertSymptom3 = conn.createStatement();
            String symptom3 = "INSERT INTO symptom VALUES(1, 'Difficulty speaking')";
            insertSymptom1.executeUpdate(symptom2);
            insertSymptom1.close();

            Statement insertSymptom4 = conn.createStatement();
            String symptom4 = "INSERT INTO symptom VALUES(1, 'Difficulty swallowing')";
            insertSymptom1.executeUpdate(symptom2);
            insertSymptom1.close();

            Statement insertSymptom5 = conn.createStatement();
            String symptom5 = "INSERT INTO symptom VALUES(1, 'Muscle spasms or cramps')";
            insertSymptom1.executeUpdate(symptom2);
            insertSymptom1.close();

            Statement insertSymptom6 = conn.createStatement();
            String symptom6 = "INSERT INTO symptom VALUES(1, 'Shortness of breath')";
            insertSymptom1.executeUpdate(symptom2);
            insertSymptom1.close();

            Statement insertSymptom7 = conn.createStatement();
            String symptom7 = "INSERT INTO symptom VALUES(1, 'Weak neck muscles')";
            insertSymptom1.executeUpdate(symptom2);
            insertSymptom1.close();

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
