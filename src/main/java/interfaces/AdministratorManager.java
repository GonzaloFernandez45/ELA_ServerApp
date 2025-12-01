package interfaces;
import pojos.Administrator;


/**
 * Interface that defines the operations for managing Administrator entities.
 *
 * Implementations (JDBCAdministratorManager)
 * will handle how administrators are stored and retrieved (DB, file, etc.).
 */
public interface AdministratorManager {
    /**
     * Inserts a new Administrator in the data source.
     *
     *
     * @param administrator Administrator object to be saved.
     * */
    public void insertAdministrator(Administrator administrator);

    /**
     * Retrieves an Administrator by their unique email.
     *
     * Used for:
     * - Login (to load the admin and check password).
     * - Checking if an email is already registered.
     *
     * @param email email of the administrator.
     * @return Administrator with that email, or null if not found (depends on implementation).
     */
    public Administrator getAdministratorByEmail(String email);

    /**
     * Retrieves an Administrator by their database ID.
     *
     * @param id primary key / unique identifier of the administrator in the DB.
     * @return Administrator with that ID.
     */
    public Administrator getAdministratorById(int id);
}
