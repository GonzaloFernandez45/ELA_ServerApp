package interfaces;
import pojos.Administrator;

public interface AdministratorManager {
    public void insertAdministrator(Administrator administrator);
    public Administrator getAdministratorByEmail(String email);
    public Administrator getAdministratorById(int id);
}
