package interfaces;

import pojos.Doctor;

import java.util.List;

/**
 * Interface that defines the operations for managing Doctor entities.
 *
 * Implementations will handle how doctors are stored and accessed
 */
public interface DoctorManager {
    /**
     * Inserts a new doctor in the data source.
     *
     *
     * @param d Doctor object to be persisted.
     */
    public void addDoctor(Doctor d);

    /**
     * Returns a list with all doctors.
     *
     * Typical use:
     *
     * @return List of all Doctor objects stored in the system.
     */
    public List<Doctor> listDoctors();

    /**
     * Retrieves the ID of a doctor given their email.
     *
     *
     * @param email doctor's email (assumed unique).
     * @return the doctor ID, or possibly -1 / exception if not found
     *         (depends on implementation).
     */
    int getDoctorIDFromEmail(String email);

    /**
     * Retrieves a doctor by their unique ID.
     *
     * @param id primary key / identifier of the doctor in the DB.
     * @return Doctor with that ID
     */
    Doctor getDoctorbyId(int id);
}
